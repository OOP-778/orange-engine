import java.util.Arrays;

public class Runner {
    public static void main(String[] args) {
        String[] split = new String[]{"prisoner", "ladderrank"};
        System.out.println(Arrays.toString(addToArray(split, 1, "")));
    }

    private static String[] addToArray(String[] split, int index, String s) {
        String[] newArray = new String[split.length + 1];
        boolean found = false;
        for (int i = 0; i < split.length + 1; i++) {
            if (i == index) {
                found = true;
                newArray[i] = s;
            } else {
                if (!found)
                    newArray[i] = split[i];
                else
                    newArray[i] = split[i - 1];
            }
        }

        return newArray;
    }
}
