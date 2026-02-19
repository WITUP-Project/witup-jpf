package br.ufpe.cin.witup.jpf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JPF listener that collects and reports conditions that may lead to thrown exceptions.
 * Works for any project. Prefers PCChoiceGenerator (jpf-symbc) for symbolic path
 * conditions when available; falls back to IntChoiceGenerator for concrete value pairs.
 */
public class ExceptionConditionListener extends ListenerAdapter {

    private static final String PC_CHOICE_GENERATOR_CLASS = "gov.nasa.jpf.symbc.numeric.PCChoiceGenerator";

    /** Strips jpf-symbc internal suffixes: value_2_SYMREAL -> value, x_1_SYMINT -> x, etc. */
    private static final Pattern SYM_SUFFIX = Pattern.compile("_(\\d+)_SYM(REAL|INT|STRING|REF)");

    /**
     * Formats a path condition string for user display:
     * - Removes "constraint # = N" header
     * - Strips _X_SYMREAL, _X_SYMINT, _X_SYMSTRING, _X_SYMREF from variable names
     * - Operators (>, <, ==, +, -, etc.) pass through unchanged
     */
    static String formatConditionForUser(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        String s = raw;
        s = s.replaceFirst("(?m)^constraint\\s*#\\s*=\\s*\\d+\\s*\\n?", "");
        s = SYM_SUFFIX.matcher(s).replaceAll("");
        return s.trim();
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

        public ExceptionSite(String thrownException, String methodSignature, int lineNumber, String condition) {
            this.thrownException = thrownException;
            this.methodSignature = methodSignature;
            this.lineNumber = lineNumber;
            this.condition = condition;
            this.conditionFriendly = formatConditionForUser(condition);
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
            Instruction insn = ti.getPC();
            if (insn != null) {
                MethodInfo methodInfo = insn.getMethodInfo();
                if (methodInfo != null) {
                    methodSignature = toReadableMethodSignature(methodInfo);
                    lineNumber = methodInfo.getLineNumber(insn);
                }
            }
            exceptionSites.add(new ExceptionSite(thrownExceptionName, methodSignature, lineNumber, symbolicCondition));
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
    @SuppressWarnings("unchecked")
    private String getPathConditionFromPCChoiceGenerator(VM vm) {
        try {
            Class<? extends ChoiceGenerator<?>> pcClass =
                    (Class<? extends ChoiceGenerator<?>>) Class.forName(PC_CHOICE_GENERATOR_CLASS);
            ChoiceGenerator<?> pcCg = vm.getLastChoiceGeneratorOfType(pcClass);
            if (pcCg == null) return null;
            Object pc = pcCg.getClass().getMethod("getCurrentPC").invoke(pcCg);
            return pc != null ? pc.toString() : null;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
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
