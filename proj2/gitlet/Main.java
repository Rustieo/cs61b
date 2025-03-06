package gitlet;


import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if(args.length==0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkNumber(args,1,1);
                try {
                    Repository.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                checkNumber(args,2,2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkNumber(args,2,2);
                Repository.commit(args[1]);
                break;
            case "branch":
                checkNumber(args,2,2);
                Repository.branch(args[1]);
                break;
            case"checkout":
                checkNumber(args,2,4);
                int length= args.length;
                if(length==3){
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String fileName=args[2];
                    Repository.checkout1(fileName);
                } else if (length==4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String commitName=args[1];
                    String fileName=args[3];
                    Repository.checkout2(commitName,fileName);
                } else if (length==2) {
                    String branchName=args[1];
                    Repository.checkout3(branchName);
                }
                break;
            case "rm":
                checkNumber(args,2,2);
                Repository.rm(args[1]);
                break;
            case "rm-branch":
                checkNumber(args,2,2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                checkNumber(args,2,2);
                Repository.reset(args[1]);
                break;
            case "log":
                checkNumber(args,1,1);
                Repository.log();
                break;
            case "global-log":
                checkNumber(args,1,1);
                Repository.globalLog();
                break;
            case "find":
                checkNumber(args,2,2);
                Repository.find(args[1]);
                break;
            case "status":
                checkNumber(args,1,1);
                Repository.status();
                break;
            case "merge":
                checkNumber(args,2,2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
    static void checkNumber(String[]args,int lo,int hi){
        if(!(args.length>=lo&&args.length<=hi)){
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
