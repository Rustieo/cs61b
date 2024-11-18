package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                try {
                    Repository.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                Repository.add(args[1]);
                // TODO: handle the `add [filename]` command
                break;
            case "commit":
                Repository.commit(args[1]);
                break;
            case"checkout":
                int length= args.length;
                if(length==3){
                    String fileName=args[2];
                    Repository.checkout1(fileName);
                } else if (length==4) {
                    String commitName=args[1];
                    String fileName=args[3];
                    Repository.checkout2(commitName,fileName);
                } else if (length==2) {
                    String branchName=args[1];
                    Repository.checkout3(branchName);
                }
                break;
        }
    }
}
