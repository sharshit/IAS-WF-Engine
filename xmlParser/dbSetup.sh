javac -cp ".:./mysql-connector-java-5.1.21.jar:./xmlParser.jar" DB_Inserts.java
cd ..
java -cp ".:./xmlParser/mysql-connector-java-5.1.21.jar:./xmlParser/xmlParser.jar" xmlParser.DB_Inserts
