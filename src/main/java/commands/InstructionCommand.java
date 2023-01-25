package commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;

public record InstructionCommand(@JsonProperty("message") String message) implements Command {

    @Serial
    public static final long serialVersionUID = 1L;
}

