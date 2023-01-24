package commands;

public record InstructionCommand(String message) implements Command {
    public static final long serialVersionUID = 1L;
}

