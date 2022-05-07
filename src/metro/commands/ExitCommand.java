package metro.commands;

public class ExitCommand implements Command {
    @Override
    public void execute(String[] args) {
        System.out.println("Goodbye!");
        System.exit(0);
    }
}
