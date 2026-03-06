import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class App {


    public static File[] documents;
    public static int totalDocuments = 0;

    // Map of token to the documents it appears in and the frequency in those documents.
    public static HashMap<String, Token> tokens = new HashMap<>();
    public static HashMap<String, Double> docTokenCount = new HashMap<>();


    public static void main(String[] args) throws IOException {

        getListOfDocuments();

        // Iterate through documents and build token frequency and document frequency maps

        for (File f : documents) {
            if(f.isDirectory() || !f.isFile()) continue;
            build(f);
        }

        generateIDFValues(totalDocuments);

        generateRTFValues();

        FULLTEST();
        // SELFTEST();
    }

    private static void printVocab() {
        System.out.println("Vocabulary:");
        for (Token t : tokens.values()) {
            System.out.println(t);
        }
    }
    // Method to test everything out.
    private static void FULLTEST() {
        fullPrint();
        for(File docA : documents) {
            for (File docB : documents) {
                cosineSimilarityWithRTFIDF(docA.getName(), docB.getName());
            }
            System.out.println();
        }
         for(File doc : documents) {
            cosineSimilarityWithRTFIDF(doc.getName());
            System.out.println();
        }
    }
    // Method to test everything out since I know we will eventually need to have options for them to look up words or compare
    private static void SELFTEST() {

        // I know we don't need this, but I went ahead and created this just as a way for us to test each feature.
        // I know eventually we will need to have some kind of UI but I just wanted to have a way to look through each one without having to scroll through lines of output.

        // Ask User options
        // 1. Print all tokens and their IDF and RTF values
        // 2. Cosine Similarity between two documents
        // 3. Which documents are most similar to a given document
        // 4. Given a word, which documents is it most relevant to (based on RTF-IDF values)
        // 5. Exit

        String option = "";
        String opt1 = "1. Print all tokens and their IDF and RTF values";
        String opt2 = "2. Cosine Similarity between two documents";
        String opt3 = "3. Which documents are most similar to a given document";
        String opt4 = "4. Given a word, which documents is it most relevant to";

        System.out.printf("\nOptions:\n%s\n%s\n%s\n%s\n%s\n", opt1, opt2, opt3, opt4, "5. Exit");

        Scanner scanner = new Scanner(System.in);
        while (!option.equals("5")) {
            System.out.print("Enter option number: ");
            option = scanner.nextLine();

            switch (option) {

                case "1" -> fullPrint();

                case "2" -> {
                    // Cosine Similarity between two documents
                    listDocuments();

                    int document1 = getValidDocumentNumber(scanner,"Enter the number of the first document: ");
                    int document2 = getValidDocumentNumber(scanner,"Enter the number of the second document: ");

                    cosineSimilarityWithRTFIDF(documents[document1 - 1].getName(),documents[document2 - 1].getName());
                }

                case "3" -> {
                    // Which documents are most similar to a given document
                    listDocuments();

                    int doc1 = getValidDocumentNumber(scanner,"Enter the number of the document you want to compare: ");

                    cosineSimilarityWithRTFIDF(documents[doc1 - 1].getName());
                }

                case "4" -> {
                    System.out.println("Not done yet.");
                }

                case "5" -> {
                    System.out.println("Exiting...");
                    scanner.close();
                }

                default -> {
                    System.out.println("Invalid option. Please try again.");
                }
            }

        }
    }

    // Helper method to get a valid document number from the user input.
    public static int getValidDocumentNumber(Scanner scanner, String prompt) {

        int docNumber = -1;

        while (docNumber < 1 || docNumber > documents.length) {
            System.out.print(prompt);
            try {
                docNumber = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        return docNumber;
    }

    // Calculate cosine similarity between two documents based on their RTF values.
    public static void cosineSimilarityWithRTFIDF(String docAName, String docBName) {

        if (!docTokenCount.containsKey(docAName) || !docTokenCount.containsKey(docBName)) {
            System.out.println("One or both documents not found.");
            return;
        }

        // FORMULA
        // cosineSimilarity = sum(A_i * B_i) / (sqrt(sum(A_i^2)) * sqrt(sum(B_i^2)))

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (Token t : tokens.values()) {
            double rtf_idfA = t.document_token_rtf.getOrDefault(docAName, 0.0);
            double rtf_idfB = t.document_token_rtf.getOrDefault(docBName, 0.0);

            dotProduct += rtf_idfA * rtf_idfB;
            magnitudeA += rtf_idfA * rtf_idfA;
            magnitudeB += rtf_idfB * rtf_idfB;
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            System.out.println("One or both documents have no tokens.");
            return;
        }

        double cosineSimilarity = dotProduct / (magnitudeA * magnitudeB);
        System.out.printf("Cosine Similarity between '%s' and '%s' : %.4f%%\n", docAName, docBName, cosineSimilarity * 100);

    }

    // Calculate cosine similarity between a given document and all other documents, and print the most similar ones.
    public static void cosineSimilarityWithRTFIDF(String docAName) {
        if (!docTokenCount.containsKey(docAName)) {
            System.out.println("No document found.");
            return;
        }

        HashMap<String, Double> similarities = new HashMap<>();

        for (File docB : documents) {
            String docBName = docB.getName();
            if (docBName.equals(docAName)) {
                continue;
            }
            double dotProduct = 0.0;
            double magnitudeA = 0.0;
            double magnitudeB = 0.0;

            for (Token t : tokens.values()) {
                double rtf_idfA = t.document_token_rtf.getOrDefault(docAName, 0.0);
                double rtf_idfB = t.document_token_rtf.getOrDefault(docBName, 0.0);

                dotProduct += rtf_idfA * rtf_idfB;
                magnitudeA += rtf_idfA * rtf_idfA;
                magnitudeB += rtf_idfB * rtf_idfB;
            }

            magnitudeA = Math.sqrt(magnitudeA);
            magnitudeB = Math.sqrt(magnitudeB);

            if (magnitudeA == 0.0 || magnitudeB == 0.0) {
                similarities.put(docBName, 0.0);
                continue;
            }

            double cosineSimilarity = dotProduct / (magnitudeA * magnitudeB);
            similarities.put(docBName, cosineSimilarity);
        }


        // Website used for sorting hashmap by value:
        // digitalocean.com/community/tutorials/java-hashmap-sort-by-value
        // Not sure exactly how this works, but I'm assuming it put all the values in an array list, we then use the collection sort method to sort the list in reverse order.
        // We then iterater through the new sorted LIST and for each of the entryies in the original hashmap, we put the key and value in the new sortedHashmap variable.
        // I still haven't exactly figured out what the .entrySet is pulling exactly but I think it should just be the "entry".
        // Either way, if it works it works right now.
        ArrayList<Double> list = new ArrayList<>();
        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : similarities.entrySet() ) {
            list.add(entry.getValue());
        }
        Collections.sort(list, Collections.reverseOrder());
        for (Double num : list) {
            for (Entry<String, Double> entry : similarities.entrySet()) {
                if (entry.getValue().equals(num)) {
                    sortedMap.put(entry.getKey(), num);
                }
            }
        }
        System.out.printf("Top 5 documents similar to '%s' :\n", docAName);
        int i = 0;
        for (Entry<String, Double> entry : sortedMap.entrySet()) {
            if (i >= 5) {
                break;
            };
            System.out.printf("-> %s: %.4f%%\n", entry.getKey(), entry.getValue() * 100);
            i++;
        }
        // End of sorting
    }

    // List all documents with their corresponding numbers for user input.
    // I know at one point he said we would need user input so this is one of the methods.
    public static void listDocuments() {
        System.out.println("Documents:");
        for(int i = 0; i < documents.length; i++) {
            System.out.println((i + 1) + ". " + documents[i].getName());
        }
    }
    
    // Load all documents from the "documents" folder and count the total number of documents.
    public static void getListOfDocuments() {
        File folder = new File("documents");
        documents = folder.listFiles();

        if (documents == null) {
            System.out.println("No documents found.");
            return;
        }

        System.out.println("Documents:");
        for (File f : documents) {
            System.out.println("- " + f.getName());
        }
    }
    
    // Removes all the html tags and converts it to lowercase.
    public static String quickCheck(String s) {
        if (s == null){
            return null;
        }

        //Remove everything between < and > including the brackets
        return s.replaceAll("<[^>]*>", "").toLowerCase();
    }
    
    // Build the token frequency and document frequency maps for a given document.
    public static void build(File document) throws IOException {
        String docName = document.getName();
        BufferedReader br = new BufferedReader( new FileReader(document));

        int lineCount = 0;
        int tokenCount = 0;
    
        String currLine;
        while ((currLine = br.readLine()) != null) {


            //This section is to clean WET file
            if(currLine.contains("WARC-Target-URI:")) {
                String[] split = currLine.split("WARC-Target-URI: ");
                if(split.length > 1) {
                    docName = split[1].trim();
                    totalDocuments++;
                }
            }
            else if(currLine.contains("WARC") || currLine.contains("Content-Type:") || currLine.contains("Content-Length:")) {
                continue;
            }


            currLine = currLine.toLowerCase();
            lineCount++;
            if(lineCount % 500000 == 0) {
                System.out.println("Line Count: " + lineCount);
                System.out.println("Tokens Processed: " + tokenCount);
            }


            if(currLine.length() == 0) {
                continue;
            }

            String tuple = "<sos>" + currLine.charAt(0);
            tokenCount++;

            docTokenCount.put(docName, docTokenCount.getOrDefault(docName, 0.0) + 1);

            if (!tokens.containsKey(tuple)) {
                tokens.put(tuple, new Token(tuple, docName));
            } else {
                tokens.get(tuple).incrementDocumentOccurance(docName);
            }

            for (int i = 0; i < currLine.length() - 1; i++) {
                tuple = currLine.charAt(i) + "" + currLine.charAt(i + 1);

                docTokenCount.put(docName, docTokenCount.getOrDefault(docName, 0.0) + 1);

                if (!tokens.containsKey(tuple)) {
                    tokens.put(tuple, new Token(tuple, docName));
                } else {
                    tokens.get(tuple).incrementDocumentOccurance(docName);
                }
                tokenCount++;
            }

            tuple = currLine.charAt(currLine.length() - 1) + "<eos>";
            tokenCount++;

            docTokenCount.put(docName, docTokenCount.getOrDefault(docName, 0.0) + 1);

            if (!tokens.containsKey(tuple)) {
                tokens.put(tuple, new Token(tuple, docName));
            } else {
                tokens.get(tuple).incrementDocumentOccurance(docName);
            }

        }

        br.close();
    }
    
    // Calculate the IDF values for all tokens based on the number of documents they appear in.
    public static void generateIDFValues(int numOfDocuments) {
        for (Token t : tokens.values()) {
            t.generateIDFValue(numOfDocuments);
        }
    }
    
    // Calculate the RTF values for all tokens in each document based on their frequency and the total word count of the document.
    public static void generateRTFValues() {
        for(Token t : tokens.values()) {
            for (String document : t.documentTokenOccurance.keySet()) {
                if(docTokenCount.get(document) == null || docTokenCount.get(document) == 0) {
                    continue;
                }

                int tokenOccurance = t.documentTokenOccurance.get(document);
                double docSize = docTokenCount.get(document);
                double rtf = tokenOccurance / docSize;
                t.document_token_rtf.put(document, rtf);
            }
        }
    }
    
    // Calculate the RTF-IDF values for all tokens in each document by multiplying their RTF values with their IDF values.
    public static void generateRTFIDFValues() {
        for(Token t : tokens.values()) {
            for (String document : t.documentTokenOccurance.keySet()) {
                if(docTokenCount.get(document) == null || docTokenCount.get(document) == 0) {
                    continue;
                }

                int tokenOccurance = t.documentTokenOccurance.get(document);
                double docSize = docTokenCount.get(document);
                double rtf = tokenOccurance / docSize;
                double rtf_idf = rtf * t.idf;
                t.document_token_rtf.put(document, rtf_idf);
            }
        }
    }

    // Print all tokens with their IDF values and the documents they appear in with their corresponding RTF values.
    public static void fullPrint() {
        for (Token t : tokens.values()) {
            System.out.println(t);
            int count = 1;
            for (String doc : t.documentTokenOccurance.keySet()) {
                System.out.printf("  Document %d -> %-24.24s: Occurrence = %d, RTF = %.4f%n", count, doc, t.documentTokenOccurance.get(doc), t.document_token_rtf.get(doc));
                count++;
            }
            System.out.println();
        }
    }
}

class Token {
    String token;
    double idf;

    // List of all the documents this token appears in and the frequency.
    HashMap<String, Integer> documentTokenOccurance = new HashMap<>();

    // List of all the documents this token appears in and the RTF value for that document.
    HashMap<String, Double> document_token_rtf = new HashMap<>();

    // Constructor
    Token(String token, String document) {
        this.token = token;
        incrementDocumentOccurance(document);
    }

    public void incrementDocumentOccurance(String document) {
        documentTokenOccurance.put(document, documentTokenOccurance.getOrDefault(document, 0) + 1);
    }

    // Posible cleaning method, but not sure if we need it or if it should be done in like a quickCheck method.
    public void clean() {
        if (token == null) {
            return;
        }

        token = token.toLowerCase().replaceAll("<[^>]*>", "");

        // Reject letters except a, o, i
        if (token.length() == 1 && 
            !(token.equals("a") || token.equals("o") || token.equals("i"))) {
            token = null;
            return;
        }
    }

    // Generates the IDF value for said token.
    // IDF FORMULA: IDF = Log_2(Total number of documents / number of documents containing the token)
    public void generateIDFValue(int totalDocuments) {
        if (documentTokenOccurance.size() == 0) {
            idf = 0;
        } else {
            idf = Math.log((double) totalDocuments / documentTokenOccurance.size()) / Math.log(2);
        }
    }

    // I felt adding the RTF value might be too much, so I just left it out and figured I could have another method output it.
    // I went ahead and commented out another section just in case we wanted to add it in later.
    public String toString() {
        return token.toUpperCase() + " (IDF: " + idf + ", Docs: " + documentTokenOccurance.size() + ")";  

        // return token.toUpperCase() + " (IDF: " + idf + ", Docs: " + document_token_occurance.size() + ")" + " RTF: " + document_token_rtf;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token other = (Token) obj;
        return Objects.equals(token, other.token);
    }

}