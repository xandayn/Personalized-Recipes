package common.xandayn.personalrecipes.client.gui;

import common.xandayn.personalrecipes.client.gui.component.GUICheckBox;
import common.xandayn.personalrecipes.client.gui.component.GUISlidingList;
import common.xandayn.personalrecipes.client.gui.recipe.RecipeGUIComponent;
import common.xandayn.personalrecipes.recipe.RecipeRegistry;
import common.xandayn.personalrecipes.util.References;
import common.xandayn.personalrecipes.util.Rendering;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @license
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Matthew DePalma
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class RecipeHandlerGUI extends GuiContainer {

    public int getGuiTop() {
        return guiTop;
    }

    /**
     * An enumeration to define what our current state is,
     * if we've just opened the inventory and haven't selected
     * a recipe handler, or if we've selected a handler to use.
     */
    private enum GUIState {
        JUST_OPENED,
        TYPE_SELECTED
    }

    private GUIState _curState;
    /**
     * A resource location pointing to the texture we want to renderBackground.
     */
    private ResourceLocation background;
    /**
     *
     */
    private RecipeGUIComponent component = null;
    private EntityPlayer player;
    private GUISlidingList slider;
    private GUICheckBox removeMode;

    //JUST_OPENED state variables
    private GuiButton selectButton;
    private GuiButton exitButton;

    private boolean startRemove;
    private String startID;

    /**
     * A basic implementation of the Container class, the only reason this
     * is here is because GuiContainer requires it, it is unused other than
     * that.
     */
    public static class RH_Container extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }

    public RecipeHandlerGUI(EntityPlayer player, String id, boolean remove) {
        super(new RH_Container());
        this.player = player;
        background = new ResourceLocation(References.MOD_ID.toLowerCase(), "textures/gui/handler_selector.png");
        _curState = GUIState.JUST_OPENED;
        xSize = 172;
        ySize = 128;
        startID = id;
        startRemove = remove;
    }

    @Override
    protected void keyTyped(char value, int keyCode) {
        switch (_curState){
            case TYPE_SELECTED:
                if(!component.keyTyped(value, keyCode)) super.keyTyped(value, keyCode);
                break;
            default:
                super.keyTyped(value, keyCode);
                break;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        if(startID == null) {
            initialize();
        } else {
            startTypeSelectedState(startID, startRemove);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        switch (_curState){
            case JUST_OPENED:
                slider.mousePressed(mouseX, mouseY, mouseButton);
                removeMode.mousePressed(mouseX, mouseY, mouseButton);
                break;
            case TYPE_SELECTED:
                component.mousePressed(mouseX, mouseY, mouseButton);
                break;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceClick);
        switch (_curState){
            case JUST_OPENED:
                slider.mousePressedAndDragged(mouseX, mouseY, mouseButton, timeSinceClick);
                break;
            case TYPE_SELECTED:
                component.mousePressedAndDragged(mouseX, mouseY, mouseButton, timeSinceClick);
                break;
        }
    }

    public void render_tool_tip(ItemStack item, int x, int y) {
        this.renderToolTip(item, x, y);
    }

    private void startTypeSelectedState(String selected, boolean remove){
        buttonList.clear();
        if(remove) {
            component = RecipeRegistry.INSTANCE.getRecipeRemoveGUIComponent(RecipeRegistry.INSTANCE.getAliasIntID(selected));
        } else {
            component = RecipeRegistry.INSTANCE.getRecipeAddGUIComponent(RecipeRegistry.INSTANCE.getAliasIntID(selected));
        }
        _curState = GUIState.TYPE_SELECTED;
        component.initGUI(this, player);
    }

    private void initialize() {
        buttonList.clear();
        _curState = GUIState.JUST_OPENED;
        component = null;
        slider = new GUISlidingList(guiLeft + 20, guiTop + 26, RecipeRegistry.INSTANCE.getRegisteredAliases().toArray(new String[RecipeRegistry.INSTANCE.registeredRecipeHandlerCount()]));
        removeMode = new GUICheckBox(guiLeft + 110, guiTop + 24, 10, true);
        removeMode.setChecked(true);
        registerGuiButton(selectButton = new GuiButton(0, guiLeft + 110, guiTop + 37, 39, 20, "Select"));
        registerGuiButton(exitButton = new GuiButton(1, guiLeft + 110, guiTop + 71, 39, 20, "Exit"));
        exitButton.enabled = false;
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseButton) {
        super.mouseMovedOrUp(mouseX, mouseY, mouseButton);
        switch (_curState){
            case JUST_OPENED:
                slider.mouseReleased(mouseX, mouseY, mouseButton);
                break;
            case TYPE_SELECTED:
                component.mouseReleased(mouseX, mouseY, mouseButton);
                break;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (_curState){
            case JUST_OPENED:
                switch (button.id){
                    case 0:
                        if(slider.getSelected() != null){
                            startTypeSelectedState(slider.getSelected(), !removeMode.isChecked());
                        }
                        break;
                    case 1:
                        player.closeScreen();
                        break;
                }
                break;
            case TYPE_SELECTED:
                component.actionPerformed(button);
                break;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        switch (_curState){
            case JUST_OPENED:
                if (exitButton != null && !exitButton.enabled) exitButton.enabled = true;
                if (removeMode.isChecked()) {
                    selectButton.enabled = slider.getSelected() != null;
                } else {
                    if(slider.getSelected() != null) {
                        int id = RecipeRegistry.INSTANCE.getAliasIntID(slider.getSelected());
                        selectButton.enabled = RecipeRegistry.INSTANCE.getRecipeRemoveGUIComponent(id) != null && RecipeRegistry.INSTANCE.getRecipeCount(id) > 0;
                    } else {
                        selectButton.enabled = false;
                    }
                }
                break;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float delta, int mouseX, int mouseY) {
        switch (_curState){
            case JUST_OPENED:
                Rendering.bindTexture(background);
                drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
                slider.update(mouseX, mouseY);
                slider.renderBackground(mouseX, mouseY);

                removeMode.update(mouseX, mouseY);
                removeMode.renderBackground(mouseX, mouseY);
                break;
            case TYPE_SELECTED:
                component.update(mouseX, mouseY);
                component.renderBackground(mouseX, mouseY);
                break;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        switch (_curState) {
            case JUST_OPENED:
                Rendering.drawString("Select a Recipe Handler", 28, 6, 0xFFDDDDDD);

                if(slider.getSelected() != null) {
                    Rendering.drawString("Selected: " + slider.getSelected(), 32, 108, 0xFFDDDDDD);
                }

                Rendering.drawString(!removeMode.isChecked() ? "Remove" : "Add", 122, 25, 0xFFDDDDDD);
                GL11.glPushMatrix();
                GL11.glTranslatef(-guiLeft, -guiTop, 0);
                slider.renderForeground(mouseX, mouseY);
                removeMode.renderForeground(mouseX, mouseY);
                GL11.glPopMatrix();
                break;
            case TYPE_SELECTED:
                GL11.glPushMatrix();
                GL11.glTranslatef(-guiLeft, -guiTop, 0);
                component.renderForeground(mouseX, mouseY);
                GL11.glPopMatrix();
                break;
        }
    }

    public void clearGuiButtons(){
        buttonList.clear();
    }

    @SuppressWarnings("unchecked")
    public void registerGuiButton(GuiButton button){
        buttonList.add(button);
    }

    public void returnToSelectScreen() {
        initialize();
    }
}
