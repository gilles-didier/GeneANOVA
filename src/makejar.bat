javac *.java
jar cvf Expression.jar *.class resources postscript/EPSGraphics.class
jar cmf mainclass.txt Expression.jar *.class resources postscript/EPSGraphics.class


