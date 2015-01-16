JAVAC=javac -d bin -classpath ./lib/guava-17.0.jar:. -Xlint:unchecked
sources = $(wildcard src/*.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f bin/*.class

%.class : %.java
	$(JAVAC) $<
