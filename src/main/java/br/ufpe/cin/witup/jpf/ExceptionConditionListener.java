package br.ufpe.cin.witup.jpf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * JPF listener that collects and reports conditions that may lead to thrown exceptions.
 * Works for any project. Prefers PCChoiceGenerator (jpf-symbc) for symbolic path
 * conditions when available; falls back to IntChoiceGenerator for concrete value pairs.
 */
public class ExceptionConditionListener extends ListenerAdapter {

    /** Strips jpf-symbc internal suffixes: value_2_SYMREAL -> value, x_1_SYMINT -> x, etc. */
    private static final Pattern SYM_SUFFIX = Pattern.compile("_(\\d+)_SYM(REAL|INT|STRING|REF)");

    /** Replaces CONST_0, CONST_1, CONST_0.0, etc. with the raw numeric value. */
    private static final Pattern CONST_PREFIX = Pattern.compile("CONST_(-?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?)");

    /**
     * Formats a path condition string for user display:
     * - Removes "constraint # = N" header
     * - Strips _X_SYMREAL, _X_SYMINT, _X_SYMSTRING, _X_SYMREF from variable names
     * - Replaces CONST_0, CONST_1, etc. with the raw value (0, 1, etc.)
     * - Qualifies instance fields as "this.fieldName" when methodInfo is available
     * - Operators (>, <, ==, +, -, etc.) pass through unchanged
     */
    static String formatConditionForUser(String raw, MethodInfo methodInfo) {
        if (raw == null || raw.isEmpty()) return raw;
        String s = raw;
        s = s.replaceFirst("(?m)^constraint\\s*#\\s*=\\s*\\d+\\s*\\n?", "");
        s = SYM_SUFFIX.matcher(s).replaceAll("");
        s = CONST_PREFIX.matcher(s).replaceAll("$1");
        s = s.trim();
        if (methodInfo != null) {
            s = qualifyInstanceFields(s, methodInfo);
        }
        return s;
    }

    /**
     * Qualifies instance field names as "this.fieldName" when they appear in the condition
     * and are not method parameters. Uses MethodInfo to distinguish parameters from fields.
     */
    private static String qualifyInstanceFields(String condition, MethodInfo methodInfo) {
        Set<String> paramNames = getParameterNames(methodInfo);
        Set<String> fieldNames = getInstanceFieldNames(methodInfo);
        if (fieldNames.isEmpty()) return condition;
        String result = condition;
        for (String fieldName : fieldNames) {
            if (!paramNames.contains(fieldName)) {
                result = result.replaceAll("\\b" + Pattern.quote(fieldName) + "\\b", "this." + fieldName);
            }
        }
        return result;
    }

    private static Set<String> getParameterNames(MethodInfo mi) {
        Set<String> names = new HashSet<>();
        LocalVarInfo[] args = mi.getArgumentLocalVars();
        if (args != null) {
            for (LocalVarInfo lv : args) {
                if (!"this".equals(lv.getName())) {
                    names.add(lv.getName());
                }
            }
        }
        return names;
    }

    private static Set<String> getInstanceFieldNames(MethodInfo mi) {
        Set<String> names = new HashSet<>();
        ClassInfo ci = mi.getClassInfo();
        while (ci != null) {
            FieldInfo[] fields = ci.getDeclaredInstanceFields();
            if (fields != null) {
                for (FieldInfo fi : fields) {
                    names.add(fi.getName());
                }
            }
            ci = ci.getSuperClass();
        }
        return names;
    }

    private List<ExceptionSite> exceptionSites = new ArrayList<>();

    static class ExceptionSite {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
        private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        private final String thrownException;
        private final String methodSignature;
        private final int lineNumber;
        private final String condition;
        private final String conditionFriendly;

        public ExceptionSite(String thrownException, String methodSignature, int lineNumber, String condition, MethodInfo methodInfo) {
            this.thrownException = thrownException;
            this.methodSignature = methodSignature;
            this.lineNumber = lineNumber;
            this.condition = condition;
            this.conditionFriendly = formatConditionForUser(condition, methodInfo);
        }

        /** User-friendly condition (e.g. "value > balance" instead of "value_2_SYMREAL > balance_1_SYMREAL"). */
        public String getConditionFriendly() {
            return conditionFriendly;
        }

        public String getCondition() {
            return condition;
        }

        public String toJson() {
            return GSON.toJson(this);
        }

        public static String toJsonArray(List<ExceptionSite> sites, boolean pretty) {
            return (pretty ? GSON_PRETTY : GSON).toJson(sites);
        }
    }

    private final List<List<Integer>> exceptionPairs = new ArrayList<>();

    @Override
    public void exceptionThrown(VM vm, gov.nasa.jpf.vm.ThreadInfo ti, gov.nasa.jpf.vm.ElementInfo thrownException) {
        String symbolicCondition = getPathConditionFromPCChoiceGenerator(vm);
        if (symbolicCondition != null) {
            String thrownExceptionName = thrownException != null ? thrownException.getClassInfo().getName() : "unknown";
            String methodSignature = "unknown";
            int lineNumber = -1;
            MethodInfo methodInfo = null;
            Instruction insn = ti.getPC();
            if (insn != null) {
                methodInfo = insn.getMethodInfo();
                if (methodInfo != null) {
                    methodSignature = toReadableMethodSignature(methodInfo);
                    lineNumber = methodInfo.getLineNumber(insn);
                }
            }
            exceptionSites.add(new ExceptionSite(thrownExceptionName, methodSignature, lineNumber, symbolicCondition, methodInfo));
            return;
        }
        return;
    }

    /** Builds a human-readable method signature, e.g. "Account.debit(double)" instead of "(D)V". */
    private static String toReadableMethodSignature(MethodInfo mi) {
        String className = mi.getClassName();
        String simpleClass = className != null && className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
        String methodName = mi.getName();
        String[] argTypes = Types.getArgumentTypeNames(mi.getSignature());
        StringBuilder sb = new StringBuilder();
        sb.append(simpleClass != null ? simpleClass : "?");
        if ("<init>".equals(methodName)) {
            sb.append('(');
        } else if ("<clinit>".equals(methodName)) {
            sb.append(".<clinit>(");
        } else {
            sb.append('.').append(methodName).append('(');
        }
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(argTypes[i]);
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * When running with jpf-symbc (Symbolic PathFinder), PCChoiceGenerator holds
     * the PathCondition for the current path. Returns its string representation
     * or null if not available.
     */
    private String getPathConditionFromPCChoiceGenerator(VM vm) {
        ChoiceGenerator<?> cg = vm.getLastChoiceGeneratorOfType(PCChoiceGenerator.class);
        if (!(cg instanceof PCChoiceGenerator)) return null;
        PCChoiceGenerator pcCg = (PCChoiceGenerator) cg;
        PathCondition pc = pcCg.getCurrentPC();
        return pc != null ? pc.toString() : null;
    }

    @Override
    public void searchFinished(Search search) {
        reportSymbolicConditions();
    }

    /**
     * Iterates over exceptionSites and prints the JSON for each exception site.
     * Uses conditionFriendly for readable output (e.g. "value > balance").
     */
    private void reportSymbolicConditions() {
        System.out.println("\n========================================");
        System.out.println("EXCEPTION CONDITIONS (symbolic path conditions):");
        for (ExceptionSite site : exceptionSites) {
            System.out.println(site.toJson());
        }
        System.out.println("========================================\n");
    }
}
