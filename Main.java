package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Daniel Bostwick
 */
public class Main {


    /** Runs the checks for Main method.
     * @param args Check arguments. */
    public static void mainChecks(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!Objects.GITLET.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /** Checks for incorrect operands.
     * @param args Check arguments. */
    public static void argsCheck(String[] args) {
        if (args.length == 4
                && !args[2].equals("--")) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt */
    public static void main(String... args) throws IOException {
        mainChecks(args);
        Objects obj = new Objects();
        switch (args[0]) {
        case "add":
            obj.add(args[1]);
            break;
        case "init":
            obj.init();
            break;
        case "commit":
            obj.commit(args[1]);
            break;
        case "global-log":
            obj.globalLog();
            break;
        case "log":
            obj.log();
            break;
        case "checkout":
            argsCheck(args);
            obj.checkout(args);
            break;
        case "find":
            obj.find(args[1]);
            break;
        case "rm":
            obj.rm(args[1]);
            break;
        case "status":
            obj.status();
            break;
        case "branch":
            obj.branch(args[1]);
            break;
        case "rm-branch":
            obj.rmBranch(args[1]);
            break;
        case "reset":
            obj.reset(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
    }
}
