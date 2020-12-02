package com.validity.monolithstarter.service;

import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

@Service
//Used to process normal.csv, and return duplicates
public class HelloService {
    public String getHelloMessage() {
        StringBuilder finalOutput =  new StringBuilder(formatInput("../test-files/normal.csv"));
        finalOutput.append(formatInput("../test-files/advanced.csv"));
        return finalOutput.toString();

    }

    //formats
    public static String formatInput(String csvFile){
        JSONArray[] result = processInput(csvFile);

        String fileName = csvFile.substring(csvFile.lastIndexOf("/")+1);
        StringBuilder output = new StringBuilder(fileName + " Duplicates: \n");

        //iterates through the set of duplicates; prints each set
        for (int i = 0; i < result[0].length();++i){
            output.append(result[0].getJSONArray(i).toString());
        }

        //prints all non-duplicates
        output.append(fileName);
        output.append(" Non-Duplicates: \n");
        for (int i = 0; i < result[1].length();++i){
            output.append(result[1].getJSONObject(i).toString());
        }
        return output.toString();
    }

    //finds the modified levenshtein distance between two strings
    public static int findModifiedLevenshtein(String str1,String str2){
        //      System.out.println(str1 + " " + str2 );
        if (str1.length() == 0 && str2.length()==0){
            return 0;
        } else if (str1.length() == 0 || str2.length()==0){ //one string is empty

            //the logic here: any case where one entry is blank and the other is not
            //should all be roughly weighted the same (one long string shouldn't be worth more than the other if
            // the other one is blank).
            //So, this code will determine the minimum of 5 and the length of the non-empty string, and return this as the distance
            return Math.min(5,Math.max(str1.length(),str2.length()));
        }

        //Both non-empty – find Levenshtein Distance using dynamic programming
        int[][] mat = new int[str2.length()+1][str1.length()+1];
        for (int i = 0; i <= str1.length();++i){
            mat[0][i]=i;
        }
        for (int i = 0; i <= str2.length(); ++i){
            mat[i][0]=i;
        }

        //iterate through strings
        for (int i = 1; i <= str2.length(); ++i){
            for (int j = 1; j <= str1.length();++j){

                //if entries aren't the same
                if (str1.charAt(j-1)!=str2.charAt(i-1)){

                    //find minimum adjacent 3 entries in matrix and add 1
                    mat[i][j]=Math.min( Math.min(mat[i-1][j-1] ,mat[i-1][j] ), mat[i][j-1])+1;
                }else{
                    //simply set it to the same as the diagonal entry
                    mat[i][j]=mat[i-1][j-1];
                }
            }
        }
        return mat[str2.length()][str1.length()];
    }

    /**
     * creates json object given string array of attributes
     * @param str1 – string array of attributes
     * @return - JSON Object
     */
    public static JSONObject createJSONObject(String[] str1){
        JSONObject obj = new JSONObject();
        obj.put("first_name",str1[1]);
        obj.put("last_name",str1[2]);
        obj.put("company",str1[3]);
        obj.put("email",str1[4]);
        obj.put("address1",str1[5]);
        obj.put("address2",str1[6]);
        obj.put("zip",str1[7]);
        obj.put("city",str1[8]);
        obj.put("state_long",str1[9]);
        obj.put("state",str1[10]);
        obj.put("phone",str1[11]);
        return obj;
    }

    public static JSONArray[] processInput(String csvFile){
        BufferedReader br = null;
        String line = "";
        String line2 = "";
        JSONArray[] toReturn = new JSONArray[2];

        JSONArray duplicateEntries=new JSONArray();
        JSONArray nonDuplicateEntries = new JSONArray();

        try{
            br = new BufferedReader(new FileReader(csvFile));
            br.readLine();
            line = br.readLine();
            String[] str1 = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
            //skip everything in double quotes

            while ((line2 = br.readLine()) != null) {
                String[] str2 = line2.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);

                int levenshteinDistance = 0;


                for (int i = 1; i < str1.length; ++i) {
                    levenshteinDistance += findModifiedLevenshtein(str1[i], str2[i]);
                }

                if (levenshteinDistance <= 35){ //roughly a quarter of average line length

                    //these are duplicates – put the duplicates in one JSON Array as a set, and attach to output
                    JSONArray duplicateEntry  = new JSONArray();
                    duplicateEntry.put(createJSONObject(str1));
                    duplicateEntry.put(createJSONObject(str2));
                    duplicateEntries.put(duplicateEntry);

                    //begin testing again with 2 new entries
                    if ((line = br.readLine()) != null){
                        str1 = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
                    }
                } else {

                    //simply add the first object to non-duplicates, and keep testing
                    nonDuplicateEntries.put(createJSONObject(str1));
                    str1 = str2;

                }
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
        toReturn[0]=duplicateEntries;
        toReturn[1]=nonDuplicateEntries;
        return toReturn;
    }


}
