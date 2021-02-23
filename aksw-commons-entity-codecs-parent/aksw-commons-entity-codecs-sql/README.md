
A framework for encoding/decoding SQL identifiers and (string) literals.
Quoting and unquoting are special cases of encoding/decoding.


Example for first setting up string codecs and subsequently the SQL codec:

```java
public class MainPlaygroundCodec {
	public static void main(String[] args) {
		StringCodec enquoter = StringCodecPrefixAndSuffix.create("\"");

		StringCodec dequoter1 = StringCodecPrefixAndSuffix.create("\"");
		StringCodec dequoter2 = StringCodecPrefixAndSuffix.create("'");
		StringCodec dequoter3 = StringCodecPrefixAndSuffix.create("`");
		
		EntityTransformCoalesce<String> compoundTransform = EntityTransformCoalesce.create(Arrays.asList(
			dequoter1.getDecoder(),
			dequoter2.getDecoder(),
			dequoter3.getDecoder()
		));
		
		EntityCodec<String> idCodec = EntityCodecImpl.create(enquoter.getEncoder(), compoundTransform);
		
		System.out.println(idCodec.encode("test"));
		// output: "test"
		
		System.out.println(idCodec.encode(idCodec.encode("test")));
		// output: ""test""

		System.out.println(idCodec.decode("'test'"));
		// output: test

		System.out.println(idCodec.decode("`test`"));
		// output: test

		System.out.println(idCodec.encode(idCodec.decode("`test`")));
		// output: "test"

		
		// Expected IllegalArgumentException if the argument cannot be decoded:
		// System.out.println(idCodec.decode("test"));
		
		
		StringCodec stringLiteralEnquoter = StringCodecPrefixAndSuffix.create("'");
		EntityCodec<String> stringLiteralCodec = EntityCodecImpl.create(
				stringLiteralEnquoter.getEncoder(),
				compoundTransform);

		// Create an sqlCodec that uses double quotes for identifier and
		// single quotes for strings
		SqlCodec sqlCodec = SqlCodecImpl.create(idCodec, stringLiteralCodec);
		
		System.out.println(sqlCodec.forTableName().encode("tableName"));
		// output: "tableName"
		
		
		System.out.println(sqlCodec.forStringLiteral().encode("I am a string"));
		// output: 'I am a string'
	}
}

```

```java

```

