## Bencode Serializer and Deserializer

[![Build Status](https://github.com/cdefgah/bencoder4j/workflows/build/badge.svg)](https://github.com/cdefgah/bencoder4j/actions) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.cdefgah/bencoder4j/badge.svg)](http://search.maven.org/#artifactdetails%7Ccom.github.cdefgah%7Cbencoder4j%7C1.1.0%7Cjar)

[Bit-torrent](https://en.wikipedia.org/wiki/Bencode) encoding and decoding implementation for the Java programming language.

### Simple use case

This project is available as a [Maven](https://maven.apache.org/) [dependency](http://search.maven.org/#artifactdetails%7Ccom.github.cdefgah%7Cbencoder4j%7C1.1.0%7Cjar) :

```xml
<dependency>
    <groupId>com.github.cdefgah</groupId>
    <artifactId>bencoder4j</artifactId>
    <version>1.1.0</version>
</dependency>
```

Below, you can see a piece of code that writes a set of arbitrary objects to the `file.ben`, located in my home `Downloads` folder. 

Bear in mind that provided examples fit my OS (Ubuntu Linux) and environment (path with my user name). Chances are that these parameters slightly differ in your case, so be sure to modify the `filePath` variable accordingly. 

```java
package example;

import com.github.cdefgah.bencoder4j.CircularReferenceException;
import com.github.cdefgah.bencoder4j.model.BencodedByteSequence;
import com.github.cdefgah.bencoder4j.model.BencodedDictionary;
import com.github.cdefgah.bencoder4j.model.BencodedInteger;
import com.github.cdefgah.bencoder4j.model.BencodedList;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriterApp {

    public static void main(String[] args) throws IOException, CircularReferenceException {

        BencodedInteger bencodedInteger = new BencodedInteger(1234567890);
        BencodedByteSequence bencodedByteSequence = new BencodedByteSequence("Hi :)");

        BencodedList bencodedList = new BencodedList();
        BencodedInteger intListElement = new BencodedInteger(1);
        BencodedByteSequence bbsListElement = new BencodedByteSequence("abc");

        bencodedList.add(intListElement);
        bencodedList.add(bbsListElement);

        BencodedDictionary bencodedDictionary = new BencodedDictionary();

        BencodedByteSequence dictKey1 = new BencodedByteSequence("111");
        BencodedByteSequence dictKey2 = new BencodedByteSequence("222");

        BencodedInteger intDictElement = new BencodedInteger(2);
        BencodedByteSequence bbsDictElement = new BencodedByteSequence("xyz");

        bencodedDictionary.put(dictKey1, intDictElement);
        bencodedDictionary.put(dictKey2, bbsDictElement);


        String filePath = "/home/rafael/Downloads/file.ben";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {

            bencodedInteger.writeObject(fos);
            bencodedByteSequence.writeObject(fos);
            bencodedList.writeObject(fos);
            bencodedDictionary.writeObject(fos);

        }
    }
}
```


Now, as we have the composed `file.ben`in the `Downloads` folder, we can use the following code to read its contents.


```java
package example;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamIterator;
import com.github.cdefgah.bencoder4j.model.BencodedObject;

import java.io.FileInputStream;
import java.io.IOException;

public class ReaderApp {
    public static void main(String[] args) throws IOException, BencodeFormatException {

        String filePath = "/home/rafael/Downloads/file.ben";
        try(FileInputStream fis = new FileInputStream(filePath)) {
            BencodeStreamIterator bis = new BencodeStreamIterator(fis);

            while (bis.hasNext()) {
                BencodedObject bencodedObject = bis.next();
                System.out.println(bencodedObject);
            }
        }
    }
}
```

Use cases given above are the most basic, simple examples. For more please refer to [the tests](https://github.com/cdefgah/bencoder4j/tree/master/src/test) for the project.

### Documentation

[API Documentation](https://cdefgah.github.io/bencoder4j/apidocs/allclasses-index.html)

[Changelog](https://github.com/cdefgah/bencoder4j/blob/master/CHANGELOG.md)

### Licensing

[MIT License](https://github.com/cdefgah/bencoder4j/blob/master/LICENSE)
