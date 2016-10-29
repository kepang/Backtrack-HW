package com.company;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.lang.StringBuilder;


/**** Notes
 *
 * 1 <= range <= 59
 * 7 lottery numbers
 * 1*7 <= number of digits <= 2*7
 *
 * str-length   2-digit-items  max-zeros
 *      7           0               0
 *      8           1               1
 *      9           2               2
 *      10          3               3
 *      11          4               4
 *      12          5               5
 *      13          6               6
 *      14          7               7
 *
 * Use str-length to predict number of 2-digit-items,
 * So str-lengths outside 7-14 are invalid (not counting leading zeros)
 *
 *
 * Goal: Examine these combinations, only if necessary:
 * C(n,r) for 1 digit and 2 digit items
 *
 * Eg. C(5,1)=5
 * 22221
 * 22212
 * 22122
 * 21222
 * 12222
 *
 *
 *
 * Initialize.
 * ----------
 *
 * Store results in a linked hashset to preserve order; O(1) lookup for duplicate check
 * Initialize helper hashset to track location of items containing zeros; O(1) lookup
 * Use a counter to track number of 2-digit items remaining
 *
 *
 *
 *
 * Recursive Backtrack.
 * -------------------
 *
 * Forward direction, best effort - give priority to:
 *
 * 1. Handle zeros first
 *      - give priority to 10, 20, 30, 40, 50 because 1, 2, 3, 4, 5, etc, have more options to fit into a lotto set
 *      - if duplicate, retry with first digit
 * 2. Handle two-digit items
 *      - finish finding required number of 2-digit items
 *      - if duplicate or out of range, retry with first digit
 * 3. Handle one-digit items
 *      - if duplicate, backtrack.
 *
 * 4. Terminate and backtrack early if string is no longer projected to fit into the remaining lotto buckets
 * 5. Successful if
 *      - 7 lottery numbers found
 *      - No duplicates, all digits used
 *      - Range 1-59
 *
 *
 *
 * Reverse direction
 *
 * 1. Backtrack -> remove items until a 2 digit item is found
 * 2. Calculate if remaining str will fit into remaining lotto buckets
 *      - Remove the 2 digit item and try its first digit
 *      - Forward recursion if successful
 *      - Continue backtrack if fail
 *
 *
 * Performance
 *
 * Time: Best: O(N), Worst: O(C(n,r)*N)
 * Space: O(N)
 *
 */



public class Main {

    private final static int LOTTO_SIZE = 7;
    private final static int UPPER_BOUND = 59;
    private final static int LOWER_BOUND = 1;
    private final static boolean DEBUG = false;

    private static int num_zeros = 0;

    private static LinkedHashSet<String> lotto_results = new LinkedHashSet<String>();
    private static HashSet<Integer> zeros_locations = new HashSet<Integer>();

    public static void main(String[] args) {

        String[] test_input = {
                "4938532894754",
                "251571534591", "25157153407021", "29150157340721",
                "5698157156", "54981571654", "50908157150", "11121100967",
                "1034067840",
                "1234564001",
                "111213987",

                "", "1", "42", "100848", "4938532894754", "1234567", "472844278465445",
                "0", "0000000", "000000000000",
                "000001234567", "0001234567",
                "123456700000", "000012345670000",
                "1234567",
                "1234564",
                "100",

                "93584723",
                "9058470310",
                "1234564001",
                "190056782",
                "00345600001278",
                "10040670910210",
                "00345600001278",
                "2350243200079920043",
                "120056789",
                "3940559403527",
                "20210002202978",
                "749229843030",
                "0010111213145958",
                "251571534591",
                "434344041489", "4343440410000000489",
                "22222222222222", "737707177"
        };

        int ptr;
        int len;
        int two_digit_counter;
        String str;

        for (String input_str : test_input) {

            lotto_results.clear();
            zeros_locations.clear();

            ptr = 0;
            num_zeros = 0;
            str = input_str.replaceFirst("^0*", ""); // Work with leading zeros removed
            len = str.length();
            two_digit_counter = len - LOTTO_SIZE; // predict number of 2 digit items

            // Skip to next input if it ends in "00" -> Minimum value is 100 > upper bound
            if (len > 2 && str.substring(len-2).equals("00")) {
                continue;
            }

            // Initialization.
            // Scan for zeros and store location if valid lotto number found: eg. 10, 20, 30, 40, 50
            // Start at index 1
            for (int i=1; i<len; i++) {
                if (str.charAt(i) == '0') {
                    int temp_val;
                    try {
                        temp_val = Integer.parseInt("" + str.charAt(i-1)) * 10;
                    }
                    catch (NumberFormatException e) {
                        temp_val = LOWER_BOUND - 1;
                    }

                    if (temp_val <= UPPER_BOUND && temp_val >= LOWER_BOUND) {
                        zeros_locations.add(i-1);
                    }
                    two_digit_counter--;
                    num_zeros++;
                }
            }


            // Main Algo with backtracking
            if (len >= LOTTO_SIZE && len <= 2 * LOTTO_SIZE + num_zeros) { // chk range 7..14

                if (find_lotto(str, ptr, two_digit_counter)) {

                    System.out.println(input_str + ": " + lotto_results.toString());

                }
                else {
                    dbug(input_str + ": No lotto found");
                }
            }
            else {
                dbug(input_str + ": Too long, short, or ends with 00");
            }
        }
    }




    private static void dbug (String str) {
        if (DEBUG) {
            System.out.println(str);
        }
    }


    // Return true - item validated
    // Return false - duplicate found OR value outside of 1-59
    private static boolean validate_item (String str) {

        int val;

        try {
            val = Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            return false;
        }

        return !(lotto_results.contains(str) || val < LOWER_BOUND || val > UPPER_BOUND);
    }



    private static boolean enter_item (String str) {

        //return validate_item(str) && lotto_results.add(str);

        if (validate_item(str)) {
            //dbug(lotto_results.toString() + " + " + str);
            return lotto_results.add(str);
        }

        return false;
    }


    private static int calc_fit (int slen, int ptr) {

        int slen_idx = slen - 1;
        return 2 * (LOTTO_SIZE - lotto_results.size())  -  (slen_idx - ptr)  +  num_zeros;
    }


    // Recursive backtracking
    private static boolean find_lotto (String str, int ptr, int two_digit_counter) {

        int inc = 1;
        int will_it_fit;
        StringBuilder temp_str = new StringBuilder();

        // Stop conditions: lotto size = 7 and EOS should be in sync, otherwise return false
        if (ptr >= str.length()) {
            return lotto_results.size() == LOTTO_SIZE;
        }
        if (lotto_results.size() == LOTTO_SIZE && two_digit_counter > 0) {
            return false;
        }


        // Fwd direction
        temp_str.append(str.charAt(ptr));
        // if value is 0, skip to next char
        try {
            if (Integer.parseInt(temp_str.toString()) < LOWER_BOUND) {
                return find_lotto(str, ptr + inc, two_digit_counter);
            }
        }
        catch (NumberFormatException e) {
            return false;
        }

        // try 2 digit (containing zeros then non-zeros)
        // try 1 digit
        // else return false
        if (zeros_locations.contains(ptr) || two_digit_counter > 0) {

            if (ptr < str.length()-1) {
                temp_str.append(str.charAt(ptr + 1));
            }
            else {
                return false;
            }

            // try two digit items
            if (validate_item(temp_str.toString())) {
                if (!zeros_locations.contains(ptr)) {
                    two_digit_counter--;
                }
                inc = 2;
            }
            else {
                if (temp_str.length() > 1) {
                    temp_str.deleteCharAt(1);
                }
                inc = 1;
            }
        }

        // Enter item into results
        if (!enter_item(temp_str.toString())) {
            return false;
        }



        // --
        // Recursion to next digits
        // Perform backtracking if necessary
        will_it_fit = calc_fit(str.length(), ptr + inc);
        dbug("fit->" + will_it_fit);

        if (will_it_fit > 0 && find_lotto(str, ptr + inc, two_digit_counter)) {
            return true;
        }

        // Fallback:
        // Backtrack on false return value for find_lotto()
        // Break apart a stored 2 digit item and try its first digit.
        // Don't recurse if remaining string won't fit into empty lotto buckets
        will_it_fit = calc_fit(str.length(), ptr + inc);
        dbug("fit<-" + will_it_fit + " on: " + temp_str);

        if (temp_str.length() == 2 && will_it_fit > 1) {
            lotto_results.remove(temp_str.toString());
            two_digit_counter++;
            temp_str.deleteCharAt(1);


            if (!enter_item(temp_str.toString())) {
                return false;
            }

            if (find_lotto(str, ptr + 1, two_digit_counter)) {
                return true;
            }

            dbug("fit<-" + will_it_fit + " on: " + temp_str);
        }

        // Dual purpose: routine removal / fallback for failed 1 digit items
        lotto_results.remove(temp_str.toString());
        return false;
    }
}

