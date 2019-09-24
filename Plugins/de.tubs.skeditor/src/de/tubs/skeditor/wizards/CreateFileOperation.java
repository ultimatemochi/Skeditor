package de.tubs.skeditor.wizards;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import SkillGraph.Category;
import SkillGraph.Graph;
import SkillGraph.Node;
import SkillGraph.SkillGraphFactory;

public class CreateFileOperation extends RecordingCommand {
	private TransactionalEditingDomain editingDomain;
	private String diagramName;
	//private URI path;
	private String containerName;

	public CreateFileOperation(TransactionalEditingDomain domain, String containerName, String name) {
		super(domain);
		this.editingDomain = domain;
		this.diagramName = name;
		//this.path = path;
		this.containerName = containerName;
	}

	@Override
	protected void doExecute() {
		String diagramTypeId = "ContractModelling";
		Diagram diagram = Graphiti.getPeCreateService().createDiagram(diagramTypeId, this.diagramName, true);
		ResourceSet rSet = editingDomain.getResourceSet();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {

		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(diagramName));

		URI uri = URI.createFileURI(file.getFullPath().toString());

		if (file == null || !file.exists()) {
			Resource createResource = rSet.createResource(uri);
			try {
				createResource.save(new HashMap());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			createResource.setTrackingModification(true);

		}
		final Resource resource2 = rSet.getResource(uri, true);

		resource2.getContents().add(diagram);

		resource2.getContents().add(SkillGraphFactory.eINSTANCE.createGraph());

		IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(diagram,
				"de.tubs.skeditor.diagram.SkillGraphDiagramTypeProvider"); //$NON-NLS-1$
		IFeatureProvider featureProvider = dtp.getFeatureProvider();
		AddContext addcontext = new AddContext();

		int dot = file.getName().lastIndexOf(".");
		Graph g = SkillGraphFactory.eINSTANCE.createGraph();

		Node node = SkillGraphFactory.eINSTANCE.createNode();
		node.setName(file.getName().substring(0, dot));
		node.setCategory(Category.MAIN);

		g.setRootNode(node);

		addcontext.setNewObject(node);

		final Resource resourceDomain = rSet.getResource(uri, true);

		resourceDomain.getContents().add(g);
		resourceDomain.getContents().add(node);

		try {
			resourceDomain.save(null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		addcontext.setTargetContainer(diagram);

		addcontext.setX(100);
		addcontext.setY(100);

		IAddFeature add = featureProvider.getAddFeature(addcontext);

		if (add.canAdd(addcontext)) {
			add.add(addcontext);
		}

		try {
			resource2.save(null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
