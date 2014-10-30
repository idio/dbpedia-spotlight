# Idio flavored DBpedia Spotlight
This is a fork of [DBpedia Spotlight](https://github.com/idio/dbpedia-spotlight/) 0.6 created at [idio](http://idio.github.io/).

#### Shedding Light on the Web of Documents

DBpedia Spotlight looks for ~3.5M things of unknown or ~320 known types in text and tries to link them to their global unique identifiers in [DBpedia](http://dbpedia.org). 

#### What are the key differences between this Fork and the main Spotlight branch ?

This fork is based on Spotlight 0.6.

Spotlight 0.7 provides a great playground for experimentation, however Spotlight 0.6 is more stable and less buggy at the moment. Due to bugs and issues with the Surface Form matching in 0.7, we have decided to maintain this 0.6 fork.

Major differences: 
- **Relevance Score**: Every spotted entitity gets a relevance score, which measures the importance of the entity within the given text.
- **Bug fixes**: A few bugs which affected 0.6 version are fixed


#### What's planned for the future ?

- Scala 2.10/2.11 support
- Models based on DBpedia 2014
- Better memory usage by Introducing the quantizied models available for Spotlight 0.7
- Improving surface form matching: lowercases, inflexions..
- Add Interface for querying/updating models

#### Installation

Compilation instructions:

1. Clone this repo
2. Inside the folder of the repo do: `mvn package`
3. Get a language model: `http://spotlight.sztaki.hu/downloads/version-0.1/`
4. Uncompress the language model
5. Run :
             ```java -Xmx15G -Xms15G -jar dist/target/dbpedia-spotlight-0.6-jar-with-dependencies.jar ~/pathToYourDecompressedModels/ http://localhost:2222/rest```

6. Go to: `http://localhost:2222/rest/annotate?text=Berlin`

## Join idio!
If you are interested in Topic Extraction, NLP or Software Engineering you should take a look at our [jobs Page](http://idio.github.io/jobs/)
We're always on the lookout for awesome people to join our team.

## Licenses

All the original code produced for DBpedia Spotlight is licensed under  [Apache License, 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Some modules have dependencies on [LingPipe](http://alias-i.com/lingpipe/) under the [Royalty Free License](http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt). Some of our original code (currently) depends on GPL-licensed or LGPL-licensed code and is therefore also GPL or LGPL, respectively. We are currently cleaning up the dependencies to release two builds, one purely GPL and one purely Apache License, 2.0.

The documentation on this website is shared as [Creative Commons Attribution-ShareAlike 3.0 Unported License](http://en.wikipedia.org/wiki/Wikipedia:Text_of_Creative_Commons_Attribution-ShareAlike_3.0_Unported_License).
