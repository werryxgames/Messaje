package com.werryxgames.messaje;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

/**
 * Class for dialog with error/warning.
 *
 * @since 1.0
 */
public class ErrorDialog extends Dialog {

  public ErrorDialog(String title, WindowStyle style) {
    super(title, style);
  }

  /**
   * Creates {@link ErrorDialog} from arguments.
   *
   * @param dialog      Instance of {@link Dialog}.
   * @param game        Instance of {@link Messaje}.
   * @param title       Title of error/warning.
   * @param description Description of error/warning.
   * @param background  Background of dialog.
   * @param buttons     List of buttons with dialog actions.
   * @return Created instance of {@link ErrorDialog}.
   */
  public static ErrorDialog fromDialog(Dialog dialog, Messaje game, String title,
      String description, Drawable background, Button... buttons) {
    ErrorDialog errorDialog = new ErrorDialog(dialog.getTitleLabel().getText().toString(),
        dialog.getStyle());
    errorDialog.center();
    errorDialog.clear();
    errorDialog.setFillParent(true);
    Label titleLabel = new Label(title, UiStyle.getLabelStyle(game.fontManager, 0, 32));
    titleLabel.setWrap(true);
    titleLabel.setAlignment(Align.center);
    errorDialog.add(titleLabel).center().width(Value.percentWidth(0.9f, errorDialog));
    errorDialog.row();
    Label descriptionLabel = new Label(description, UiStyle.getLabelStyle(game.fontManager, 0, 24));
    descriptionLabel.setWrap(true);
    descriptionLabel.setAlignment(Align.center);
    errorDialog.add(descriptionLabel).center().width(Value.percentWidth(0.9f, errorDialog));
    errorDialog.row();
    Table buttonsTable = new Table();

    for (Button button : buttons) {
      buttonsTable.add(button);
    }

    buttonsTable.pack();
    errorDialog.add(buttonsTable);
    errorDialog.pack();
    return errorDialog;
  }
}
