package com.example.nkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;

import android.util.Log;

import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
/*//import android.os.Bundle;
//import android.text.InputType;
//import android.text.method.MetaKeyKeyListener;
//import android.view.KeyCharacterMap;
//import android.view.inputmethod.CompletionInfo;
//import java.util.ArrayList;
*/import java.util.List;

public class SoftKeyBoard extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener {

	static final boolean DEBUG = false;
	private InputMethodManager mInputMethodManager;
	private LatinKeyBoardView mInputView;
	private CandidateView mCandidateView;
	private StringBuilder mComposing = new StringBuilder();
	//private boolean mPredictionOn;
	private int mLastDisplayWidth;
	private LatinKeyBoard mQwertyKeyboard;
	private String mWordSeparators;
	private boolean mEncryptionStatus = false;
	
	
	/**
	 * Main initialization of the input method component. Be sure to call to
	 * super class.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mWordSeparators = getResources().getString(R.string.word_separators);
	}

	/**
	 * This is the point where you can do all of your UI initialization. It is
	 * called after creation and any configuration change.
	 */
	@Override
	public void onInitializeInterface() {
		if (mQwertyKeyboard != null) {
			// Configuration changes can happen after the keyboard gets
			// recreated,
			// so we need to be able to re-build the keyboards if the available
			// space has changed.
			int displayWidth = getMaxWidth();
			if (displayWidth == mLastDisplayWidth)
				return;
			mLastDisplayWidth = displayWidth;
		}
		mQwertyKeyboard = new LatinKeyBoard(this, R.xml.qwerty);
	}

	/**
	 * Called by the framework when your view for creating input needs to be
	 * generated. This will be called the first time your input method is
	 * displayed, and every time it needs to be re-created such as due to a
	 * configuration change.
	 */
	@Override
	public View onCreateInputView() {
		mInputView = (LatinKeyBoardView) getLayoutInflater().inflate(
				R.layout.input, null);
		Log.d("testing", "test");
		mInputView.setOnKeyboardActionListener(this);
		mInputView.setKeyboard(mQwertyKeyboard);
		return mInputView;
	}

	/**
	 * Called by the framework when your view for showing candidates needs to be
	 * generated, like {@link #onCreateInputView}.
	 */
	@Override
	public View onCreateCandidatesView() {
		mCandidateView = new CandidateView(this);
		mCandidateView.setService(this);
		return mCandidateView;
	}

	/**
	 * This is the main point where we do our initialization of the input method
	 * to begin operating on an application. At this point we have been bound to
	 * the client, and are now receiving all of the detailed information about
	 * the target of our edits.
	 */
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);

		// Reset our state. We want to do this even if restarting, because
		// the underlying state of the text editor could have changed in any
		// way.
		mComposing.setLength(0);
		updateCandidates();

		//mPredictionOn = false;

		// Update the label on the enter key, depending on what the application
		// says it will do.
		mQwertyKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

	/**
	 * This is called when the user is done editing a field. We can use this to
	 * reset our state.
	 */
	@Override
	public void onFinishInput() {
		super.onFinishInput();

		// Clear current composing text and candidates.
		mComposing.setLength(0);
		// updateCandidates();

		// We only hide the candidates window when finishing input on
		// a particular editor, to avoid popping the underlying application
		// up and down if the user is entering text into the bottom of
		// its window.
		setCandidatesViewShown(false);

		// mCurKeyboard = mQwertyKeyboard;
		if (mInputView != null) {
			mInputView.closing();
		}
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		super.onStartInputView(attribute, restarting);
		// Apply the selected keyboard to the input view.
		mInputView.setKeyboard(mQwertyKeyboard);
		mInputView.closing();
		final InputMethodSubtype subtype = mInputMethodManager
				.getCurrentInputMethodSubtype();
		mInputView.setSubtypeOnSpaceKey(subtype);
	}

	@Override
	public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
		mInputView.setSubtypeOnSpaceKey(subtype);
	}

	/**
	 * Deal with the editor reporting movement of its cursor.
	 */
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);

		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if (mComposing.length() > 0
				&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			mComposing.setLength(0);
			updateCandidates();
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}

	/**
	 * This translates incoming hard key events in to edit operations on an
	 * InputConnection. It is only needed when using the PROCESS_HARD_KEYS
	 * option.
	 */
	/*
	 * private boolean translateKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * }
	 */

	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * }
	 */

	/**
	 * Use this to monitor key events being delivered to the application. We get
	 * first crack at them, and can either resume them or let them continue to
	 * the app.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Helper function to commit any text being composed in to the editor.
	 */
	private void commitTyped(InputConnection inputConnection) {
		if (mComposing.length() > 0) {
			inputConnection.commitText(mComposing, mComposing.length());
			mComposing.setLength(0);
			updateCandidates();
		}
	}

	/**
	 * Helper to determine if a given character code is alphabetic.
	 */
	private boolean isAlphabet(int code) {
		if (Character.isLetter(code)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Helper to send a key down / key up pair to the current editor.
	 */
	private void keyDownUp(int keyEventCode) {
		
		  getCurrentInputConnection().sendKeyEvent( new
		  KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		  getCurrentInputConnection().sendKeyEvent( new
		  KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
		 
	}

	/**
	 * Helper to send a character to the editor as raw key events.
	 */
	private void sendKey(int keyCode) {
		switch (keyCode) {
		case '\n':
			keyDownUp(KeyEvent.KEYCODE_ENTER);
			break;
		default:
			if (keyCode >= '0' && keyCode <= '9') {
				keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
			} else {
				getCurrentInputConnection().commitText(
						String.valueOf((char) keyCode), 1);
			}
			break;
		}
	}

	// Implementation of KeyboardViewListener

	public void onKey(int primaryCode, int[] keyCodes)
	{
		if (isWordSeparator(primaryCode)) 
		{
			// Handle separator
			if (mComposing.length() > 0) 
			{
				commitTyped(getCurrentInputConnection());
			}
			sendKey(primaryCode);

		}
		else if (primaryCode == Keyboard.KEYCODE_DELETE)
		{
			handleBackspace();
		} 
		else if (primaryCode == -1) 
		{
			setEncryptionStatus();
		}
		/*else if (primaryCode == Keyboard.KEYCODE_SHIFT) 
		{
			handleShift();
		}*/
		else if (primaryCode == Keyboard.KEYCODE_CANCEL) 
		{
			handleClose();
			return;
		} 
		else
		{
			handleCharacter(primaryCode, keyCodes);
		}
	}

	public void setEncryptionStatus() {
		if (mEncryptionStatus == false) {
			mComposing.append("Encryption On");
			getCurrentInputConnection().setComposingText(mComposing, 1);
			mEncryptionStatus = true;
		} else {
			mComposing.append("Encryption Off");
			getCurrentInputConnection().setComposingText(mComposing, 1);
			mEncryptionStatus = false;
		}
	}

	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		ic.beginBatchEdit();
		if (mComposing.length() > 0) {
			commitTyped(ic);
		}
		ic.commitText(text, 0);
		ic.endBatchEdit();
		// updateShiftKeyState(getCurrentInputEditorInfo());
	}

	/**
	 * Update the list of available candidates from the current composing text.
	 * This will need to be filled in by however you are determining candidates.
	 */
	private void updateCandidates() {
	}

	public void setSuggestions(List<String> suggestions, boolean completions,
			boolean typedWordValid) {
		if (suggestions != null && suggestions.size() > 0) {
			setCandidatesViewShown(true);
		} else if (isExtractViewShown()) {
			setCandidatesViewShown(true);
		}
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(suggestions, completions,
					typedWordValid);
		}
	}

	private void handleBackspace() {
		final int length = mComposing.length();
		if (length > 1) {
			mComposing.delete(length - 1, length);
			getCurrentInputConnection().setComposingText(mComposing, 1);
			//updateCandidates();
		} else if (length > 0) {
			mComposing.setLength(0);
			getCurrentInputConnection().commitText("", 0);
			//updateCandidates();
		} else {
			keyDownUp(KeyEvent.KEYCODE_DEL);
		}
		// updateShiftKeyState(getCurrentInputEditorInfo());
	}

	/*private void handleShift() {
		
	}*/

	private void handleCharacter(int primaryCode, int[] keyCodes) {
			if (isAlphabet(primaryCode) ) {
			mComposing.append((char) primaryCode);
			getCurrentInputConnection().setComposingText(mComposing, 1);
			// updateShiftKeyState(getCurrentInputEditorInfo());
			//updateCandidates();
		} else {
			getCurrentInputConnection().commitText(
					String.valueOf((char) primaryCode), 1);
		}
	}

	private void handleClose() {
		commitTyped(getCurrentInputConnection());
		requestHideSelf(0);
		mInputView.closing();
	}

	private String getWordSeparators() {
		return mWordSeparators;
	}

	public boolean isWordSeparator(int code) {
		String separators = getWordSeparators();
		return separators.contains(String.valueOf((char) code));
	}

	public void pickDefaultCandidate() {
		// pickSuggestionManually(0);
	}

	public void swipeRight() {
	}

	public void swipeLeft() {
		handleBackspace();
	}

	public void swipeDown() {
		handleClose();
	}

	public void swipeUp() {
	}

	public void onPress(int primaryCode) {
	}

	public void onRelease(int primaryCode) {
	}
}