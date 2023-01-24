package commands;

import java.math.BigInteger;

public record ResultCommand(BigInteger prime, String senderId) implements Command {
    public static final long serialVersionUID = 1L;
}
