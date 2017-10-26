javac *.java
jar cvf Expression.jar *.class *.java resources
jar cmf mainclass.txt Expression.jar *.class *.java resources

