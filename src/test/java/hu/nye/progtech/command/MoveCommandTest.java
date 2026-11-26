package hu.nye.progtech.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import hu.nye.progtech.model.Board;
import hu.nye.progtech.model.GameState;
import hu.nye.progtech.model.Move;
import hu.nye.progtech.service.BoardService;
import hu.nye.progtech.ui.BoardRenderer;
import hu.nye.progtech.ui.ConsoleView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoveCommandTest {

    @Mock
    private BoardService boardService;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final ConsoleView view = new ConsoleView(new ByteArrayInputStream(new byte[0]),
            new PrintStream(output, true), new BoardRenderer());

    @Test
    void machineIsSkippedWhenItCannotMove() {
        when(boardService.getBoard()).thenReturn(new Board(1, 2, List.of()));
        when(boardService.winner()).thenReturn(Optional.empty());
        when(boardService.doARandomMachineMove()).thenReturn(Optional.empty());
        MoveCommand command = new MoveCommand(view);
        Matcher matcher = command.match("move a 1");
        matcher.matches();

        command.apply(new GameState(boardService), matcher);

        verify(boardService).doMove(new Move('A', 1));
        assertTrue(output.toString(Charset.defaultCharset()).contains("kimarad"));
    }
}
