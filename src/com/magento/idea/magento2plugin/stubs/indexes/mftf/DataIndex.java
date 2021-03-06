package com.magento.idea.magento2plugin.stubs.indexes.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DataIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create(
        "com.magento.idea.magento2plugin.stubs.indexes.mftf.data_index"
    );

    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> map = new THashMap<>();
            PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            XmlDocument xmlDocument = ((XmlFile) psiFile).getDocument();

            if (xmlDocument == null) {
                return map;
            }

            XmlTag xmlRootTag = xmlDocument.getRootTag();

            if (xmlRootTag == null || !xmlRootTag.getName().equals("entities")) {
                return map;
            }

            XmlTag xmlTags[] = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);

            if (xmlTags == null) {
                return map;
            }

            for (XmlTag entityTag : xmlRootTag.findSubTags("entity")) {
                String entityName = entityTag.getAttributeValue("name");

                if (entityName == null || entityName.isEmpty()) {
                    continue;
                }

                map.put(entityName, entityName);

                for (XmlTag dataTag : entityTag.findSubTags("data")) {
                    String key = dataTag.getAttributeValue("key");
                    String value = dataTag.getText();

                    if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
                        continue;
                    }

                    map.put(entityName + "." + key, value);
                }
            }

            return map;
        };
    }

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return this.myKeyDescriptor;
    }

    @NotNull
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file ->
                file.getFileType() == XmlFileType.INSTANCE &&
                file.getPath().contains("Test/Mftf/Data")
            ;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
