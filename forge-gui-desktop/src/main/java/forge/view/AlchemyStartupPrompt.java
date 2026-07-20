package forge.view;

import javax.swing.JOptionPane;

import forge.gui.FThreads;
import forge.localinstance.properties.ForgeConstants;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.model.FModel;
import forge.util.Localizer;

/**
 * Asks, once per launch, whether Alchemy (MTG Arena digital-only) cards should be part of
 * this session.
 *
 * <p>This has to run before {@code FModel.initialize()} builds the card database, because the
 * Alchemy exclusion happens while the pool is assembled and that pool is memoized for the rest
 * of the run. It therefore also has to set up the {@link Localizer} itself, so the question can
 * be shown in the user's configured language before Forge's own start-up sequence does it.</p>
 */
final class AlchemyStartupPrompt {
    private AlchemyStartupPrompt() { }

    static void ask() {
        final Localizer localizer = Localizer.getInstance();
        localizer.initialize(FModel.getPreferences().getPref(FPref.UI_LANGUAGE), ForgeConstants.LANG_DIR);

        // Last saved answer becomes the pre-selected button.
        final boolean useAlchemyByDefault = FModel.getPreferences().getPrefBoolean(FPref.UI_LOAD_ALCHEMY_CARDS);

        final String title = localizer.getMessageorUseDefault("lblAlchemyCardsTitle", "Alchemy Cards");
        final String question = localizer.getMessageorUseDefault("lblUseAlchemyCardsThisSession",
                "Include Alchemy cards (MTG Arena digital-only cards) in this session?");
        final String yes = localizer.getMessageorUseDefault("lblYes", "Yes");
        final String no = localizer.getMessageorUseDefault("lblNo", "No");

        final boolean[] useAlchemy = { useAlchemyByDefault };
        FThreads.invokeInEdtAndWait(() -> {
            final Object[] options = { yes, no };
            final int choice = JOptionPane.showOptionDialog(null, question, title,
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, useAlchemyByDefault ? yes : no);
            if (choice == 0 || choice == 1) { //dialog dismissed with the X keeps the default
                useAlchemy[0] = (choice == 0);
            }
        });

        FModel.setLoadAlchemyCardsForSession(useAlchemy[0]);
    }
}
