package net.dasdarklord.componenteditor.screen.widgets.suggestor;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract and modified version of the vanilla {@link net.minecraft.client.gui.screen.ChatInputSuggestor}
 */
public abstract class TextWidgetSuggestor {

    protected final TextRenderer textRenderer;
    protected final MinecraftClient client;
    protected CompletableFuture<Suggestions> pendingSuggestions;
    protected SuggestionWindow window;
    protected final ParentElement owner;
    protected final TextFieldWidget textField;
    protected boolean windowAbove;
    protected boolean completingSuggestions;
    protected int maxSuggestionSize;
    protected int inWindowIndexOffset;
    protected int color;

    public TextWidgetSuggestor(MinecraftClient client, ParentElement owner, TextFieldWidget textField, int maxSuggestionSize, int inWindowIndexOffset, boolean windowAbove, int color) {
        this.client = client;
        this.textRenderer = client.textRenderer;
        this.owner = owner;
        this.textField = textField;
        this.windowAbove = windowAbove;
        this.maxSuggestionSize = maxSuggestionSize;
        this.inWindowIndexOffset = inWindowIndexOffset;
        this.color = color;
        textField.setRenderTextProvider(this::provideRenderText);
    }

    public abstract void refresh();

    public void show() {
        if (pendingSuggestions != null && pendingSuggestions.isDone()) {
            Suggestions suggestions = pendingSuggestions.join();
            if (!suggestions.isEmpty()) {
                int width = 0;
                for(Suggestion suggestion : suggestions.getList()) {
                    width = Math.max(width, this.textRenderer.getWidth(suggestion.getText()));
                }

                List<Suggestion> suggestionList = suggestions.getList();
                window = new SuggestionWindow(textField.getX(), textField.getY(), width, suggestionList, false);
            } else clearWindow();
        } else if (pendingSuggestions == null) clearWindow();
    }

    protected OrderedText provideRenderText(String original, int firstCharacterIndex) {
        return OrderedText.styledForwardsVisitedString(original, Style.EMPTY);
    }

    @Nullable
    static String getSuggestionSuffix(String original, String suggestion) {
        return suggestion.startsWith(original) ? suggestion.substring(original.length()) : null;
    }

    public SuggestionWindow getWindow() {
        return window;
    }

    public void clearWindow() {
        window = null;
        textField.setSuggestion(null);
    }

    public boolean tryRenderWindow(DrawContext context, int mouseX, int mouseY) {
        if (window != null) {
            window.render(context, mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (window != null && window.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (owner.getFocused() != this.textField || keyCode != GLFW.GLFW_KEY_TAB) {
            return false;
        } else {
            show();
            return true;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return window != null && window.mouseScrolled(mouseX, mouseY, MathHelper.clamp(amount, -1.0F, 1.0F));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return window != null && window.mouseClicked((int)mouseX, (int)mouseY, button);
    }

    public class SuggestionWindow {

        private final Rect2i area;
        private final String typedText;
        private final List<Suggestion> suggestions;
        private int inWindowIndex;
        private int selection;
        private Vec2f mouse;
        boolean completed;

        SuggestionWindow(final int x, final int y, final int width, final List<Suggestion> suggestions, final boolean narrateFirstSuggestion) {
            this.mouse = Vec2f.ZERO;
            int i = x - (TextWidgetSuggestor.this.textField.drawsBackground() ? 0 : 1);
            int j = TextWidgetSuggestor.this.windowAbove ? y - 2 - Math.min(suggestions.size(), TextWidgetSuggestor.this.maxSuggestionSize) * 12 : y - (TextWidgetSuggestor.this.textField.drawsBackground() ? 1 : 0) + 2 + TextWidgetSuggestor.this.textField.getHeight();
            this.area = new Rect2i(i, j, width + 1, Math.min(suggestions.size(), TextWidgetSuggestor.this.maxSuggestionSize) * 12);
            this.typedText = TextWidgetSuggestor.this.textField.getText();
            this.suggestions = suggestions;
            this.select(0);
        }

        public void render(DrawContext context, int mouseX, int mouseY) {
            int i = Math.min(this.suggestions.size(), TextWidgetSuggestor.this.maxSuggestionSize);
            int j = -5592406;
            boolean bl = this.inWindowIndex > 0;
            boolean bl2 = this.suggestions.size() > this.inWindowIndex + i;
            boolean bl3 = bl || bl2;
            boolean bl4 = this.mouse.x != (float)mouseX || this.mouse.y != (float)mouseY;
            if (bl4) {
                this.mouse = new Vec2f((float)mouseX, (float)mouseY);
            }

            if (bl3) {
                context.fill(this.area.getX(), this.area.getY() - 1, this.area.getX() + this.area.getWidth(), this.area.getY(), TextWidgetSuggestor.this.color);
                context.fill(this.area.getX(), this.area.getY() + this.area.getHeight(), this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight() + 1, TextWidgetSuggestor.this.color);
                if (bl) {
                    for(int k = 0; k < this.area.getWidth(); ++k) {
                        if (k % 2 == 0) {
                            context.fill(this.area.getX() + k, this.area.getY() - 1, this.area.getX() + k + 1, this.area.getY(), -1);
                        }
                    }
                }

                if (bl2) {
                    for(int k = 0; k < this.area.getWidth(); ++k) {
                        if (k % 2 == 0) {
                            context.fill(this.area.getX() + k, this.area.getY() + this.area.getHeight(), this.area.getX() + k + 1, this.area.getY() + this.area.getHeight() + 1, -1);
                        }
                    }
                }
            }

            boolean bl5 = false;

            for(int l = 0; l < i; ++l) {
                Suggestion suggestion = this.suggestions.get(l + this.inWindowIndex);
                context.fill(this.area.getX(), this.area.getY() + 12 * l, this.area.getX() + this.area.getWidth(), this.area.getY() + 12 * l + 12, TextWidgetSuggestor.this.color);
                if (mouseX > this.area.getX() && mouseX < this.area.getX() + this.area.getWidth() && mouseY > this.area.getY() + 12 * l && mouseY < this.area.getY() + 12 * l + 12) {
                    if (bl4) {
                        this.select(l + this.inWindowIndex);
                    }

                    bl5 = true;
                }

                context.drawTextWithShadow(TextWidgetSuggestor.this.textRenderer, suggestion.getText(), this.area.getX() + 1, this.area.getY() + 2 + 12 * l, l + this.inWindowIndex == this.selection ? -256 : -5592406);
            }

            if (bl5) {
                Message message = (this.suggestions.get(this.selection)).getTooltip();
                if (message != null) {
                    context.drawTooltip(TextWidgetSuggestor.this.textRenderer, Texts.toText(message), mouseX, mouseY);
                }
            }

        }

        public boolean mouseClicked(int x, int y, int button) {
            if (!this.area.contains(x, y)) {
                return false;
            } else {
                int i = (y - this.area.getY()) / 12 + this.inWindowIndex;
                if (i >= 0 && i < this.suggestions.size()) {
                    this.select(i);
                    this.complete();
                }

                return true;
            }
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (this.area.contains((int) mouseX, (int) mouseY)) {
                this.inWindowIndex = MathHelper.clamp((int)((double)this.inWindowIndex - amount), 0, Math.max(this.suggestions.size() - TextWidgetSuggestor.this.maxSuggestionSize, 0));
                return true;
            } else {
                return false;
            }
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                this.scroll(-1);
                this.completed = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
                this.scroll(1);
                this.completed = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_TAB) {
                if (this.completed) {
                    this.scroll(Screen.hasShiftDown() ? -1 : 1);
                }

                this.complete();
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                TextWidgetSuggestor.this.clearWindow();
                TextWidgetSuggestor.this.textField.setSuggestion(null);
                return true;
            } else {
                return false;
            }
        }

        public void scroll(int offset) {
            this.select(this.selection + offset);
            int i = this.inWindowIndex;
            int j = this.inWindowIndex + TextWidgetSuggestor.this.maxSuggestionSize - 1;
            if (this.selection < i) {
                this.inWindowIndex = MathHelper.clamp(this.selection, 0, Math.max(this.suggestions.size() - TextWidgetSuggestor.this.maxSuggestionSize, 0));
            } else if (this.selection > j) {
                this.inWindowIndex = MathHelper.clamp(this.selection + TextWidgetSuggestor.this.inWindowIndexOffset - TextWidgetSuggestor.this.maxSuggestionSize, 0, Math.max(this.suggestions.size() - TextWidgetSuggestor.this.maxSuggestionSize, 0));
            }

        }

        public void select(int index) {
            this.selection = index;
            if (this.selection < 0) {
                this.selection += this.suggestions.size();
            }

            if (this.selection >= this.suggestions.size()) {
                this.selection -= this.suggestions.size();
            }

            Suggestion suggestion = this.suggestions.get(this.selection);
            TextWidgetSuggestor.this.textField.setSuggestion(TextWidgetSuggestor.getSuggestionSuffix(TextWidgetSuggestor.this.textField.getText(), suggestion.apply(this.typedText)));
        }

        public void complete() {
            Suggestion suggestion = this.suggestions.get(this.selection);
            TextWidgetSuggestor.this.completingSuggestions = true;
            TextWidgetSuggestor.this.textField.setText(suggestion.apply(this.typedText));
            int i = suggestion.getRange().getStart() + suggestion.getText().length();
            TextWidgetSuggestor.this.textField.setSelectionStart(i);
            TextWidgetSuggestor.this.textField.setSelectionEnd(i);
            this.select(this.selection);
            TextWidgetSuggestor.this.completingSuggestions = false;
            this.completed = true;
        }

    }

}
