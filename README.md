# Bencode Serializer and Deserializer

[![Build Status](https://travis-ci.org/cdefgah/bencoder4j.svg?branch=master)](https://travis-ci.org/cdefgah/bencoder4j)

[Bit-torrent] encoding and decoding implementation for the Java programming language.



## Simple use case

Install the artifact to the local maven repository via the following command from the project folder:

```bash
mvn clean package install
```

Create a new Java Maven project in IDE of your choice and specify the following dependency in `pom.xml`

```xml
<dependency>
	<groupId>com.github.cdefgah</groupId>
	<artifactId>bencoder4j</artifactId>
	<version>1.0.0</version>
</dependency>
```

Below, there is a piece of code that writes a set of arbitrary objects to the `file.ben`, located in my home `Downloads` folder. 

Please note this example assumes you are running the code in Linux environment and note my Linux user name in the provided path. As you have another user name in Linux or you are running example in another operating system, please change value for `filePath` variable accordingly.

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

The provided use cases are the simplest ones. For more use cases look at the tests for the project.




## Licensing

The project is distributed under [MIT License](https://opensource.org/licenses/MIT).

[Bit-Torrent]: https://en.wikipedia.org/wiki/Bencode
