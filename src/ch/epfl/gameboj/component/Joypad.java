package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class Joypad implements Component {

	private enum P1 implements Bit {
		COL_0, COL_1, COL_2, COL_3, SELECT_ROW_0, SELECT_ROW_1;
	}
	
	public enum Key {
		RIGHT, LEFT, UP, DOWN, A, B, SELECT, START;
		
		public boolean firstRow() { return (ordinal() / BUTTONS_PER_ROW) == 0; }
		public int column() { return ordinal() % BUTTONS_PER_ROW; }
	}
	
	private final Cpu cpu;
	
	private int p1 = 0;
	private int firstRow = 0;
	private int secondRow = 0;
	
	private static final int BUTTONS_PER_ROW = 4;
	private static final int SELECT_MASK = P1.SELECT_ROW_0.mask() | P1.SELECT_ROW_1.mask();
	
	private static final P1[] P1_COLUMNS = {P1.COL_0, P1.COL_1, P1.COL_2, P1.COL_3};
	private static final P1[] P1_ROWS = {P1.SELECT_ROW_0, P1.SELECT_ROW_1};
	
	public Joypad(Cpu cpu) {
		this.cpu = Objects.requireNonNull(cpu);
	}
	
	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		
		if (address == AddressMap.REG_P1) {
			updateColumns();
			return Bits.complement8(p1);
		}
		
		return Component.NO_DATA;
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		
		if (address == AddressMap.REG_P1) {
			 updateRows(Bits.complement8(data));
		}
	}
	
	public void keyPressed(Key k) {
		changeKeyState(k, true);
	}
	
	public void keyReleased(Key k) {
		changeKeyState(k, false);
	}
	
	private void changeKeyState(Key k, boolean value) {
		if (k.firstRow()) {
			firstRow = Bits.set(firstRow, k.column(), value);
		} else {
			secondRow = Bits.set(secondRow, k.column(), value);
		}
	}
	
	private void updateRows(int selectedRows) {
		int p1WithoutRows = (p1 & ~SELECT_MASK);
		p1 = p1WithoutRows | (selectedRows & SELECT_MASK);
	}
	
	private void updateColumns() {
		for (P1 column : P1_COLUMNS) {
			updateColumn(column);
		}
	}
	
	private void updateColumn(P1 column) {
		boolean currentState = Bits.test(p1, column);
		boolean newState = newColumnState(column);
		
		if (!currentState && newState) {
			cpu.requestInterrupt(Cpu.Interrupt.JOYPAD);
		}
		
		p1 = Bits.set(p1, column.index(), newState);
	}
	
	private boolean newColumnState(P1 column) {
		boolean newState = false;
		
		for (P1 row : P1_ROWS) {
			newState = newState || keyState(column, row) && Bits.test(p1, row);
		}
		
		return newState;
	}
	
	private boolean keyState(P1 column, P1 row) {
		int target = (row == P1.SELECT_ROW_0) ? firstRow : secondRow;
		return Bits.test(target, column);
	}
}
