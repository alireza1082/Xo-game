# Production R8 / ProGuard rules for XO Game

# Retain line number and source file information for crash stack trace deobfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Enums are restored from Bundle state by name/ordinal; keep their valueOf/values entry points.
-keepclassmembers enum ir.sharif.xo.engine.* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
