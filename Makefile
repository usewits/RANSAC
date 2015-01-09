JAVAC=javac -d bin -Xlint:unchecked
sources = $(wildcard src/*.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f bin/*.class

%.class : %.java
	$(JAVAC) $<
