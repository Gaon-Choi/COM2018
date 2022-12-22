package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Gaon Choi
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                // do not require any arguments except the first arg

                // e.g. java gitlet.Main init
                Utils.validateNumArgs("init", args, 1);
                Repository.init();
                break;

            case "add":
                // requires the filename

                // e.g. java gitlet.Main add hello.txt
                Utils.validateNumArgs("add", args, 2);
                String filename = args[1];
                Repository.add(filename);
                break;

            case "commit":
                // requires the commit message

                // e.g. java gitlet.Main commit "added .txt file"
                Utils.validateNumArgs("commit", args, 2);
                String commMsg = args[1];
                Repository.commit(commMsg);
                break;

            case "rm":
                // requires the file name to be deleted

                // e.g. java gitlet.Main rm hello.txt
                Utils.validateNumArgs("rm", args, 2);
                String rmname = args[1];
                Repository.rm(rmname);
                break;

            case "log":
                // do not require any arguments except the first arg

                // e.g. java gitlet.Main log
                Utils.validateNumArgs("log", args, 1);
                Repository.log();
                break;

            case "global-log":
                // do not require any arguments except the first arg

                // e.g. java gitlet.Main global-log
                Utils.validateNumArgs("global-log", args, 1);
                Repository.global_log();
                break;

            case "find":
                // requires the commit message

                // java gitlet.Main find "added .txt file"
                Utils.validateNumArgs("find", args, 2);
                String message = args[1];
                Repository.find(message);
                break;

            case "status":
                // do not require any arguments except the first arg

                // java gitlet.Main status
                Utils.validateNumArgs("status", args, 1);
                Repository.status();
                break;

            case "checkout":
                if (args.length == 2) {
                    // java gitlet.Main checkout [branch name]
                    String bname = args[1];
                    Repository.checkoutBranch(bname);
                } else if (args.length == 3) {
                    // java gitlet.Main checkout -- [file name]
                    String fname = args[2];

                    if (args[1].equals("--") && fname.length() > 0) {
                        Repository.checkoutFile(fname);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                } else if (args.length == 4) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    String commitId = args[1];
                    String fname = args[3];

                    if (args[2].equals("--") && fname.length() > 0 && commitId.length() > 0) {
                        Repository.checkoutFileCommit(commitId, fname);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;

            case "branch":
                // requires the branch name

                // e.g. java gitlet.Main branch fix-bug
                Utils.validateNumArgs("branch", args, 2);
                Repository.branch(args[1]);
                break;

            case "rm-branch":
                // requires the branch name to be deleted

                // e.g. java gitlet.Main rm-branch fix-bug
                Utils.validateNumArgs("rm-branch", args, 2);
                Repository.rm_branch(args[1]);
                break;

            case "reset":
                Utils.validateNumArgs("reset", args, 2);
                Repository.reset(args[1]);
                break;

            case "merge":
                Utils.validateNumArgs("merge", args, 2);
                Repository.merge(args[1]);
                break;

//            case "test":
//                Utils.validateNumArgs("test", args, 3);
//                Repository.test(args[1], args[2]);
//                break;

            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
