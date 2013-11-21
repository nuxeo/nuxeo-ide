/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ide.common.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nuxeo.ide.common.forms.model.ButtonWidget;
import org.nuxeo.ide.common.forms.model.CheckBoxWidget;
import org.nuxeo.ide.common.forms.model.ComboWidget;
import org.nuxeo.ide.common.forms.model.DirectoryChooserWidget;
import org.nuxeo.ide.common.forms.model.DynamicTableWidget;
import org.nuxeo.ide.common.forms.model.FileChooserWidget;
import org.nuxeo.ide.common.forms.model.FillLayoutManager;
import org.nuxeo.ide.common.forms.model.FramePanel;
import org.nuxeo.ide.common.forms.model.GridLayoutManager;
import org.nuxeo.ide.common.forms.model.HorizontalLine;
import org.nuxeo.ide.common.forms.model.LinkWidget;
import org.nuxeo.ide.common.forms.model.ListBoxWidget;
import org.nuxeo.ide.common.forms.model.NotNullValidator;
import org.nuxeo.ide.common.forms.model.Panel;
import org.nuxeo.ide.common.forms.model.PasswordWidget;
import org.nuxeo.ide.common.forms.model.RadioGroupWidget;
import org.nuxeo.ide.common.forms.model.RadioWidget;
import org.nuxeo.ide.common.forms.model.RegexValidator;
import org.nuxeo.ide.common.forms.model.SashWidget;
import org.nuxeo.ide.common.forms.model.TextAreaWidget;
import org.nuxeo.ide.common.forms.model.TextBoxWidget;
import org.nuxeo.ide.common.forms.model.TextWidget;
import org.nuxeo.ide.common.forms.model.ToggleButtonWidget;
import org.nuxeo.ide.common.forms.model.ToolbarWidget;
import org.nuxeo.ide.common.forms.model.VerticalLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Form {

    protected Map<String, Class<?>> widgetTypes;

    protected Map<String, LayoutManager> layouts;

    protected Map<String, UIObject<?>> bindings;

    protected Map<String, UIObject<?>> widgets;

    protected Map<String, ActionHandler> actions;

    protected Map<String, String> errors;

    protected Map<String, Class<? extends Validator>> validators;

    protected ErrorHandler errorHandler;

    protected ImageProvider imageProvider;

    protected Map<String, Image> images;

    public Form() {
        layouts = new HashMap<String, LayoutManager>();
        widgetTypes = new HashMap<String, Class<?>>();
        bindings = new HashMap<String, UIObject<?>>();
        widgets = new HashMap<String, UIObject<?>>();
        actions = new HashMap<String, ActionHandler>();
        errors = new HashMap<String, String>();
        validators = new HashMap<String, Class<? extends Validator>>();
        images = new HashMap<String, Image>();
        initDefaults();
    }

    protected void initDefaults() {
        addLayoutManager(new FillLayoutManager());
        addLayoutManager(new GridLayoutManager());

        addWidgetType(ToolbarWidget.class);
        addWidgetType(SashWidget.class);
        addWidgetType(HorizontalLine.class);
        addWidgetType(VerticalLine.class);
        addWidgetType(Panel.class);
        addWidgetType(FramePanel.class);
        addWidgetType(TextWidget.class);
        addWidgetType(LinkWidget.class);
        addWidgetType(ButtonWidget.class);
        addWidgetType(CheckBoxWidget.class);
        addWidgetType(RadioWidget.class);
        addWidgetType(RadioGroupWidget.class);
        addWidgetType(ToggleButtonWidget.class);
        addWidgetType(TextBoxWidget.class);
        addWidgetType(PasswordWidget.class);
        addWidgetType(TextAreaWidget.class);
        addWidgetType(ListBoxWidget.class);
        addWidgetType(ComboWidget.class);
        addWidgetType(FileChooserWidget.class);
        addWidgetType(DirectoryChooserWidget.class);
        addWidgetType(DynamicTableWidget.class);

        addValidator("regex", RegexValidator.class);
        addValidator("required", NotNullValidator.class);
    }

    public void addValidator(String type, Class<? extends Validator> clazz) {
        validators.put(type, clazz);
    }

    public Class<? extends Validator> getValidator(String type) {
        return validators.get(type);
    }

    @SuppressWarnings("unchecked")
    public Validator getValidatorInstance(String type) {
        try {
            Class<? extends Validator> clazz = getValidator(type);
            if (clazz == null) {
                clazz = (Class<? extends Validator>) Class.forName(type);
            }
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("No such validator: " + type);
        }
    }

    public Map<String, Class<? extends Validator>> getValidators() {
        return validators;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void addLayoutManager(LayoutManager layoutManager) {
        layouts.put(layoutManager.getId(), layoutManager);
    }

    public Map<String, LayoutManager> getLayouts() {
        return layouts;
    }

    public LayoutManager getLayoutManager(String id) {
        return layouts.get(id);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void addError(UIObject<?> obj, String error) {
        if (errorHandler == null) {
            return;
        }
        errors.put(obj.getId(), error);
        errorHandler.showError(obj, error);
        errorHandler.setErrorCount(errors.size());
    }

    public void removeError(UIObject<?> obj) {
        if (errorHandler == null) {
            return;
        }
        errors.remove(obj.getId());
        errorHandler.hideError(obj);
        if (!errors.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = errors.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                errorHandler.showError(getWidget(entry.getKey()),
                        entry.getValue());
            }
        }
        errorHandler.setErrorCount(errors.size());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, Class<?>> getWidgetTypes() {
        return widgetTypes;
    }

    public <T extends UIObject<?>> void addWidgetType(Class<T> type) {
        WidgetName wa = type.getAnnotation(WidgetName.class);
        if (wa == null) {
            throw new IllegalArgumentException(
                    "No @WidgetType annotation is present on class " + type);
        }
        widgetTypes.put(wa.value(), type);
    }

    @SuppressWarnings("rawtypes")
    public UIObject<?> newWidget(String type) {
        Class clazz = widgetTypes.get(type);
        if (clazz == null) {
            return null;
        }
        try {
            return (UIObject<?>) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UIObject<?> getBinding(Element element) {
        UIObject<?> obj = null;
        String type = element.getNodeName();
        if ("widget".equals(type)) {
            obj = bindings.get(UIObject.getAttribute(element, "id"));
        } else {
            obj = newWidget(type);
        }
        if (obj == null) {
            // TODO create an error widget?
            throw new IllegalArgumentException("Unable to bind widget "
                    + element.getNodeName() + " with ID "
                    + UIObject.getAttribute(element, "id"));
        }
        return obj;
    }

    public void addBinding(String id, UIObject<?> binding) {
        bindings.put(id, binding);
    }

    public Map<String, UIObject<?>> getBindings() {
        return bindings;
    }

    public UIObject<?> getBinding(String id) {
        return bindings.get(id);
    }

    public UIObject<?> getWidget(String id) {
        return widgets.get(id);
    }

    public Control getWidgetControl(String id) {
        UIObject<?> obj = widgets.get(id);
        return obj != null ? obj.getControl() : null;
    }

    public String getWidgetValueAsString(String id) {
        UIObject<?> obj = getWidget(id);
        if (obj instanceof HasValue) {
            return ((HasValue) obj).getValueAsString();
        }
        return null;
    }

    /**
     * @since 1.2
     */
    public Boolean getWidgetValueAsBoolean(String id) {
        UIObject<?> obj = getWidget(id);
        if (obj instanceof HasValue) {
            return Boolean.parseBoolean(((HasValue) obj).getValueAsString());
        }
        return null;
    }

    public boolean setWidgetValueIfEmpty(String id, Object value) {
        UIObject<?> obj = getWidget(id);
        if (obj instanceof HasValue) {
            String v = ((HasValue) obj).getValueAsString();
            if (v.length() == 0) {
                ((HasValue) obj).setValue(value);
                return true;
            }
        }
        return false;
    }

    public boolean setWidgetValue(String id, Object value) {
        UIObject<?> obj = getWidget(id);
        if (obj instanceof HasValue) {
            ((HasValue) obj).setValue(value);
        }
        return false;
    }

    public Object getWidgetValue(String id) {
        UIObject<?> obj = getWidget(id);
        if (obj instanceof HasValue) {
            return ((HasValue) obj).getValue();
        }
        return null;
    }

    public Map<String, UIObject<?>> getWidgets() {
        return widgets;
    }

    public UIObject<?> load(Composite parent, URL url) throws Exception {
        InputStream in = url.openStream();
        try {
            return load(parent, in);
        } finally {
            in.close();
        }
    }

    public UIObject<?> load(Composite parent, File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        try {
            return load(parent, in);
        } finally {
            in.close();
        }
    }

    public UIObject<?> load(Composite parent, InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);
        return load(parent, document.getDocumentElement());
    }

    public UIObject<?> load(Composite parent, Element element) {
        UIObject<?> obj = getBinding(element);
        BindingContext ctx = new BindingContext(this);
        obj.bind(parent, element, ctx);
        return obj;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setValues(Properties values) {
        setValues((Map) values);
    }

    public void setValues(Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            UIObject<?> obj = getWidget(entry.getKey());
            if (obj instanceof HasValue) {
                ((HasValue) obj).setValue(entry.getValue());
            }
        }
    }

    public Map<String, String> getValues() {
        return getValues(false);
    }

    public Map<String, String> getValues(boolean doValidation) {
        HashMap<String, String> values = new HashMap<String, String>();
        for (UIObject<?> obj : widgets.values()) {
            if (obj instanceof HasValue) {
                String v = ((HasValue) obj).getValueAsString();
                values.put(obj.getId(), v);
            }
        }
        return values;
    }

    public void validate() {
        for (UIObject<?> obj : widgets.values()) {
            if (obj instanceof HasValue) {
                ((HasValue) obj).validate();
            }
        }
    }

    public void handleAction(String id, UIObject<?> obj, Object event) {
        ActionHandler handler = actions.get(id);
        if (handler != null) {
            handler.handleAction(this, obj, event);
        }
    }

    public void addActionHandler(String id, ActionHandler handler) {
        actions.put(id, handler);
    }

    public Map<String, ActionHandler> getActionHandlers() {
        return actions;
    }

    public void setImageProvider(ImageProvider imageProvider) {
        this.imageProvider = imageProvider;
    }

    public ImageProvider getImageProvider() {
        return imageProvider;
    }

    public Map<String, Image> getImages() {
        return images;
    }

    public Image getImage(String key) {
        Image img = images.get(key);
        if (img == null && imageProvider != null) {
            img = imageProvider.getImage(key);
            if (img != null) {
                images.put(key, img);
            }
        }
        return img;
    }

    public void dispose() {
        if (!images.isEmpty()) {
            for (Image img : images.values()) {
                img.dispose();
            }
        }
        images = null;
    }
}
