package cste.misc;

import android.text.InputType;
import android.text.method.NumberKeyListener;

public class HexKeyListener extends NumberKeyListener{

	@Override
	public int getInputType() {
		return InputType.TYPE_CLASS_TEXT;
	}

	@Override
	protected char[] getAcceptedChars() {
		// TODO Auto-generated method stub
		return new char [] { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
                
                'A', 'B', 'C', 'D', 'E', 'F'};
	}

}
