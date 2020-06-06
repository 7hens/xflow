val result = StringBuilder()
val params = java.util.HashMap<String, String>()
for (i in 1..9) {
    params["P$i"] = "p$i";
    val paramTypes = params.keys.joinToString(", ")
    val paramPairs = params.entries.map { it.key + " " + it.value }.joinToString(", ")
    val paramNames = params.values.joinToString(", ")
    result.append("""
        
        public static <$paramTypes> 
        void call(Action$i<$paramTypes> action, 
            $paramPairs) {
            try {
                action.invoke($paramNames);
            } catch (Throwable e) {
                onError(e);
            }
        }
        
        public static <$paramTypes> 
        void call(Iterable<Action$i<$paramTypes>> actions,
                $paramPairs) {
            try {
                for (Action$i<$paramTypes> action : actions) {
                    action.invoke($paramNames);
                }
            } catch (Throwable e) {
                onError(e);
            }
        }
        
    """.trimIndent())
}

result.append("// =================================================")

params.clear()

for (i in 1..9) {
    params["P$i"] = "p$i";
    val paramTypes = params.keys.joinToString(", ")
    val paramPairs = params.entries.map { it.key + " " + it.value }.joinToString(", ")
    val paramNames = params.values.joinToString(", ")
    result.append("""
        
        public static <$paramTypes, R> 
        R call(Func$i<$paramTypes, R> func,
            $paramPairs) {
            try {
                return func.invoke($paramNames);
            } catch (Throwable e) {
                return onError(e);
            }
        }
        
    """.trimIndent())
}

println(result)

