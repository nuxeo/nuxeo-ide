${copyright}

package ${package};

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * ${authorTag}
 */

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@Deploy({
    <#list projectIds as projectId>
	"${projectId}",
    </#list>
	})
public class ${className} {

    @Inject
    CoreSession coreSession;

    <#list docTypes as docType>
    @Test
    public void test${docType.id}() throws Exception {
        // Create a document under the repository root
        // So far the document will be stored in memory
        DocumentModel ${docType.id} = coreSession.createDocumentModel("/", "${docType.label}",
                "${docType.id}");
        Assert.assertNotNull(${docType.id});
        // Mark the document for creation in database
        // To improve performances all pending changes will be written at the same time
        // When calling coreSession.save()
        ${docType.id} = coreSession.createDocument(${docType.id});
        // Save all pending changes on documents in the database
        // This method is only needed in the context of a unit test
        // Anywhere else, it will be called automatically
        coreSession.save();
        Assert.assertNotNull(${docType.id});
    }
    </#list>

}
