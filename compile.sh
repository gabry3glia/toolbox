# get file name
read -p "Enter sketch \"*.java\" file name: " filename

# compile engine
javac -d bin/ $(find toolbox -name "*.java")
# compile sketch
javac -d bin/ $filename.java

# fill the manifest file
echo Main-Class: $filename > MANIFEST.MF

# make jar
jar cvfm $filename.jar MANIFEST.MF -C bin .

# remove "bin" folder (containing all compiled "*.class" files)
rm -rf bin
# remove manifest file
rm MANIFEST.MF
