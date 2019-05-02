# How to run the code:
To run the code in command line:
1) pull/clone the code from: github.com/rohansinha16/Watson
2) download the index folder from (it includes 6 indices so it’s 3.76 GB): https://drive.google.com/file/d/1a6zP2Id01Ow9iGngDItK6XVy2mZHJ3kp/view?usp=sharing
3) put index folder in the pulled/cloned folder
4) navigate to the location of the folder from cmd line/terminal
5) type the following:
  >`$ mvn compile`</p>
  >`$ mvn test`</p>
<p>If done correctly, for each index in the index folder (a total of six), a test will run that calculates the precision at 1 (P@1) and mean reciprocal rank (MMR) using four scoring methods:</p>
:  - Vector Space Model and tf/idf (default)</p>
:  - Boolean Model</p>
:  - BM25 Model</p>
:  - Jelinek Mercer Model (post due date edit: with 0.5 smoothing value)</p>

6) Optionally you can run a single test with the following command:
>`$ mvn -Dtest=<TestName> test`</p>
<p>The test names are Test<Capitalized Index Name>, where the index names can be found at the end of the “Indexing and Retrieval” section, along with comments about their different properties. To test the none or lemmaNoTpl indices, the following commands would be used:</p>
> ` $ mvn -Dtest=TestNone test`</p>
> ` $ mvn -Dtest=TestLemmaNoTpl test`</p>

# The Code:
There are four classes in this project. They are all documented fairly thouroghly.
Watson.java – includes main function, generally calls QueryEngine or IndexCreator. Has several settings as variables that will be passed to QueryEngine/IndexCreator for easy use of the program. However, all indices included in the linked index folder already have a test associated with them and can be run from part 6 of the instructions above. These settings should be useful if you'd like to test something different.
## File settings:
	String indexPath - index location name, used by both
	String queries – queries filename, used by QueryEngine
	String input_dir – wiki files folder. used by IndexCreator
## Settings:
	boolean useLemma – should index/queries use lemmas or not, used by both.
	boolean useStem – should index/queries use stems or not, used by both.
	boolean removeTpl – should tpl tags be removed, used by IndexCreator
## Functionality Settings:
	boolean createIndex – should an index be created by IndexCreator
	boolean runQueries – should queries be tested by QueryEngine
