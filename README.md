# How to run the code:
To run the code in command line.
1) pull/clone the code from: github.com/rohansinha16/Watson
2) download the index folder from (it includes 6 indices so it’s not small):
3) put index folder in the pulled/cloned folder
4) navigate to the location of the folder from cmd line/terminal
5) type the following:
$ mvn compile
$ mvn test

If done correctly, for each index in the index folder (a total of six), a test will run that calculates the precision at 1 (P@1) and mean reciprocal rank (MMR) using four scoring methods:
Vector Space Model and tf/idf (default)
Boolean Model
BM25 Model
Jelinek Mercer Model

# The Code:
There are four classes in this project. They are all documented fairly thouroghly.
Watson.java – includes main function, generally calls QueryEngine or IndexCreator. Has several settings as variables that will be passed to QueryEngine/IndexCreator for easy use of the program.
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
