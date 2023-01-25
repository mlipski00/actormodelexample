package commands;

import java.io.Serial;
import java.math.BigInteger;

public record ResultCommand(BigInteger prime, String senderId) implements Command {

    @Serial
    public static final long serialVersionUID = 1L;
}
