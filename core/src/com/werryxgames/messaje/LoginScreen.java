package com.werryxgames.messaje;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.jcajce.provider.digest.SHA3.Digest256;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

/**
 * Screen for logging in to account.
 *
 * @since 1.0
 */
public class LoginScreen extends DefaultScreen {

  static final byte[] PEPPER = Utils.hexToBytes(Config.get("password.pepper", "69D029BE4D8E0C42"));

  /**
   * Default constructor for {@code DefaultScreen}.
   *
   * @param game Instance of {@link Messaje}.
   * @since 1.0
   */
  public LoginScreen(Messaje game) {
    super(game);
  }

  @Override
  void onUpdate(float delta) {
  }

  @Override
  void onResize(int width, int height) {

  }

  @Override
  public void init() {
    Table table2 = new Table();
    Label loginLabel = new Label(
        "Login:",
        UiStyle.getLabelStyle(this.game.fontManager, 0, 24)
    );
    table2.add(loginLabel).padRight(4).right();
    TextField loginField = new TextField(
        "",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24)
    );
    table2.add(loginField).padLeft(4).width(220);
    table2.row();
    Label passwordLabel = new Label(
        "Password:",
        UiStyle.getLabelStyle(this.game.fontManager, 0, 24)
    );
    table2.add(passwordLabel).padRight(4).right();
    TextField passwordField = new TextField(
        "",
        UiStyle.getTextFieldStyle(this.game.fontManager, 0, 24)
    );
    passwordField.setPasswordMode(true);
    passwordField.setPasswordCharacter('•');
    table2.add(passwordField).padLeft(4).width(220);
    table2.pack();

    Table table3 = new Table();
    TextButton registerButton = new TextButton(
        "Register",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24)
    );
    registerButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        LoginScreen.this.game.logger.fine("Register button pressed");

        byte[] hashedPassword;
        String textPassword = passwordField.getText();
        byte[] textPasswordBytes = textPassword.getBytes(StandardCharsets.UTF_8);
        String loginText = loginField.getText();
        byte[] loginBytes = loginText.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = ByteBuffer.allocate(
                textPasswordBytes.length + loginBytes.length + LoginScreen.PEPPER.length)
            .put(textPasswordBytes).put(loginBytes).put(LoginScreen.PEPPER).array();

        DigestSHA3 sha3 = new Digest256();
        sha3.update(passwordBytes);
        hashedPassword = sha3.digest();

        if (!LoginScreen.this.checkData(loginText, loginBytes, textPassword)) {
          return;
        }

        int loginLength = loginBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(2 + 1 + loginLength + hashedPassword.length);
        buffer.putShort((short) 0);
        buffer.put((byte) loginLength);
        buffer.put(loginBytes);
        buffer.put(hashedPassword);
        LoginScreen.this.game.client.send(buffer);
      }
    });
    table3.add(registerButton).width(150).padRight(4);
    TextButton logInButton = new TextButton(
        "Log in",
        UiStyle.getTextButtonStyle(this.game.fontManager, 0, 24)
    );
    logInButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        LoginScreen.this.game.logger.fine("Log in button pressed");

        byte[] hashedPassword;
        String textPassword = passwordField.getText();
        byte[] textPasswordBytes = textPassword.getBytes(StandardCharsets.UTF_8);
        String loginText = loginField.getText();
        byte[] loginBytes = loginText.getBytes(StandardCharsets.UTF_8);
        byte[] passwordBytes = ByteBuffer.allocate(
                textPasswordBytes.length + loginBytes.length + LoginScreen.PEPPER.length)
            .put(textPasswordBytes).put(loginBytes).put(LoginScreen.PEPPER).array();

        DigestSHA3 sha3 = new Digest256();
        sha3.update(passwordBytes);
        hashedPassword = sha3.digest();

        if (!LoginScreen.this.checkData(loginText, loginBytes, textPassword)) {
          return;
        }

        int loginLength = loginBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(2 + 1 + loginLength + hashedPassword.length);
        buffer.putShort((short) 1);
        buffer.put((byte) loginLength);
        buffer.put(loginBytes);
        buffer.put(hashedPassword);
        LoginScreen.this.game.client.send(buffer);
      }
    });
    table3.add(logInButton).width(150).padLeft(4);
    table3.pack();

    Table panelTable = new Table();
    Label titleLabel = new Label(
        "Log in to account or create new",
        UiStyle.getLabelStyle(this.game.fontManager, 1, 30)
    );
    panelTable.add(titleLabel).padBottom(8);
    panelTable.row();
    panelTable.add(table2).padTop(4).padBottom(8);
    panelTable.row();
    panelTable.add(table3);
    panelTable.pack();

    Table table = new Table();
    table.add(panelTable).width(panelTable.getWidth() + 40).height(panelTable.getHeight() + 30);
    table.setFillParent(true);
    table.pack();

    table.setBackground(this.colorToDrawable(new Color(0x121212ff)));

    NinePatch panelNinePatch = new NinePatch(
        ResourceLoader.loadTexture(Gdx.files.internal("ui" + File.separator + "panel_bg.png")), 14,
        14, 14, 14);
    panelTable.setBackground(new NinePatchDrawable(panelNinePatch));

    this.game.stage.addActor(table);
  }

  /**
   * Checks login and password lengths. If something is wrong, shows warning.
   *
   * @param login      Entered login.
   * @param loginBytes Byte array of entered login.
   * @param password   Entered password.
   * @return Returns {@code true} if data is correct, {@code false} otherwise.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean checkData(String login, byte[] loginBytes, String password) {
    int loginLength = login.length();

    if (loginBytes.length > 64 || loginLength > 16) {
      this.warning("Incorrect login", "Length of login should be at most 16");
      return false;
    }

    if (loginLength < 3) {
      this.warning("Incorrect login", "Length of login should be at least 3");
      return false;
    }

    int passwordLength = password.length();

    if (passwordLength < 6) {
      this.warning("Unsecure password", """
          Length of password should be at least 6 (recommended is randomly generated 16+ \
          characters)
          Also use different passwords for different applications""");
      return false;
    }

    return true;
  }

  @Override
  protected void onMessageMain(int code, ByteBuffer message) {
    switch (code) {
      case 0, 6 -> this.changeScreen(new ContactsScreen(this.game));
      case 1 -> this.warning("Unknown error", "Unknown error occurred in server");
      case 2 -> this.warning("Login already used", "Account with specified login already exists");
      case 3 -> this.warning("Unknown login length", "Login is too short or too long");
      case 4 -> {
        byte titleLength = message.get();
        byte[] titleBytes = new byte[titleLength];
        message.get(titleBytes);
        String title = new String(titleBytes, StandardCharsets.UTF_8);
        short messageLength = message.getShort();
        byte[] messageBytes = new byte[messageLength];
        message.get(messageBytes);
        String messageString = new String(messageBytes, StandardCharsets.UTF_8);
        this.warning(title, messageString);
      }
      case 5 -> this.warning("Invalid login or password",
          "Account with specified login and password doesn't exist");
      default -> this.warning("Invalid message from server",
          "Received message, that shouldn't be received");
    }
  }

  @Override
  public void onReconnect() {

  }

  @Override
  public void onDisconnect() {
    this.game.logger.fine("Disconnected from server");
  }

  @Override
  public void show() {

  }

  @Override
  public void pause() {

  }

  @Override
  public void resume() {

  }

  @Override
  public void hide() {

  }

  @Override
  public void onDispose() {
  }
}
