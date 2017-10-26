import java.beans.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

class DesignANOVADialog extends JDialog {
	private static final int stepNumber = 0;
	private static final int stepFactor = 1;
	private static final int stepAtom = 2;
	private TableExp tableExp;
	private JButton buttonOK = new JButton(okLabel), buttonCancel = new JButton(cancelLabel);
	private JPanel curPanel, totalPanel = new JPanel();
	private int n, state, curFactor, nFactors, nAtoms, curAtom;
	private JOptionPane optionPane;
	private final NumberfactorPropPane numberfactorPropPane = new NumberfactorPropPane();
	private final FactorPropPane factorPropPane = new FactorPropPane();
	private final AtomSelectionPane atomSelectionPane = new AtomSelectionPane();
	private Factor factor;
	private ANOVADesign anova;
	private Action setAction;
	private static String okLabel;
	private static String cancelLabel;
	private static String askNumber;
	private static String partitionAllRowsLabel;
	private static String partitionAllColsLabel;
	private static String partitionRowsLabel;
	private static String partitionColsLabel;
	private static String selectedLabel;
	private static String unselectedLabel;
	private static String nameLabel;
	private static String dialogTitle;
	private static String numberTitle;
	private static String factorTitle;
	private static String atomTitle;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.DesignANOVADialog", Locale.getDefault());
			okLabel = resources.getString("okLabel");
			cancelLabel = resources.getString("cancelLabel");
			askNumber = resources.getString("askNumber");
			partitionAllRowsLabel = resources.getString("partitionAllRowsLabel");
			partitionAllColsLabel = resources.getString("partitionAllColsLabel");
			partitionRowsLabel = resources.getString("partitionRowsLabel");
			partitionColsLabel = resources.getString("partitionColsLabel");
			selectedLabel = resources.getString("selectedLabel");
			unselectedLabel = resources.getString("unselectedLabel");
			nameLabel = resources.getString("nameLabel");
			dialogTitle = resources.getString("dialogTitle");
			numberTitle = resources.getString("numberTitle");
			factorTitle = resources.getString("factorTitle");
			atomTitle = resources.getString("atomTitle");
		} catch (MissingResourceException mre) {
			okLabel = "OK";
			cancelLabel = "Cancel";
			askNumber = "Number of factors : ";
			partitionAllRowsLabel = "Partitions all rows";
			partitionAllColsLabel = "Partitions all columns";
			partitionRowsLabel = "Partitions rows";
			partitionColsLabel = "Partitions columns";
			selectedLabel = "Selected";
			unselectedLabel = "Unselected";
			nameLabel = "Name";
			dialogTitle = "Design ANOVA";
			numberTitle = "Numbers of factors";
			factorTitle = "Set factor ";
			atomTitle = "Atom number ";
		}
	}
	public DesignANOVADialog(TableExp tE, Action set) {
		super();
		setAction = set;
		tableExp = tE;
		setSize(500, 400);
		setResizable(false);
		Object[] options = {okLabel, cancelLabel};
		setTitle(dialogTitle);
		Object [] mess = {numberTitle, numberfactorPropPane};
		optionPane = new JOptionPane(mess, 
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
		state = stepNumber;
        	setContentPane(optionPane);
        	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});
		optionPane.addPropertyChangeListener(new MyPropertyChangeListener());
	}
		

	public void readNumber() {
		state = stepNumber;
		curPanel = new NumberfactorPropPane();
		totalPanel.add("Center", curPanel);
	}
	public void readFactor() {
		state = stepFactor;
		totalPanel.remove(curPanel);
		curPanel = new NumberfactorPropPane();
		totalPanel.add("Center", curPanel);
	}
	
	class NumberfactorPropPane extends JPanel {
		NumberField numField = new NumberField(false, false, 2, 4);
		public NumberfactorPropPane() {
			add(new JLabel(askNumber));
			add(numField);
		}
		public int getValue() {
			return numField.intValue();
		}
	}
	
	class FactorPropPane extends JPanel {
		JTextField nameField = new JTextField(20);
		NumberField rowField = new NumberField(false, false, 2, 4);
		NumberField colField = new NumberField(false, false, 2, 4);
		JRadioButton radio1 = new JRadioButton(partitionAllRowsLabel);
		JRadioButton radio2 = new JRadioButton(partitionAllColsLabel);
		JRadioButton radio3 = new JRadioButton(partitionRowsLabel);
		JRadioButton radio4 = new JRadioButton(partitionColsLabel);
		public FactorPropPane() {
			ButtonGroup group = new ButtonGroup();
			group.add(radio1);
			group.add(radio2);
			group.add(radio3);
			group.add(radio4);
			rowField.setMaximumSize(rowField.getPreferredSize());
			colField.setMaximumSize(colField.getPreferredSize());
			nameField.setMaximumSize(nameField.getPreferredSize());
			JPanel paneName = new JPanel();
			paneName.setLayout(new BoxLayout(paneName, BoxLayout.X_AXIS));
			paneName.add(new JLabel(nameLabel));
			paneName.add(Box.createRigidArea(new Dimension(5,0)));
			paneName.add(nameField);
			JPanel paneRow = new JPanel();
			paneRow.setLayout(new BoxLayout(paneRow, BoxLayout.X_AXIS));
			radio3.setAlignmentY(Component.CENTER_ALIGNMENT);
			rowField.setAlignmentY(Component.CENTER_ALIGNMENT);
			paneRow.add(radio3);
			paneRow.add(rowField);
			JPanel paneCol = new JPanel();
			paneCol.setLayout(new BoxLayout(paneCol, BoxLayout.X_AXIS));
			radio4.setAlignmentY(Component.CENTER_ALIGNMENT);
			colField.setAlignmentY(Component.CENTER_ALIGNMENT);
			paneCol.add(radio4);
			paneCol.add(colField);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			radio1.setAlignmentX(LEFT_ALIGNMENT);
			radio2.setAlignmentX(LEFT_ALIGNMENT);
			paneRow.setAlignmentX(LEFT_ALIGNMENT);
			paneCol.setAlignmentX(LEFT_ALIGNMENT);
			paneName.setAlignmentX(LEFT_ALIGNMENT);
			radio1.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			radio2.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			paneRow.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			paneCol.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			paneName.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			radio1.setSelected(true);
			add(paneName);
			add(Box.createRigidArea(new Dimension(0,20)));
			add(radio1);
			add(radio2);
			add(paneRow);
			add(paneCol);
		}
		public int getState() {
			if(radio1.isSelected())
				return Factor.ALL_ROWS;
			if(radio2.isSelected())
				return Factor.ALL_COLUMNS;
			if(radio3.isSelected())
				return Factor.PARTIAL_ROWS;
			if(radio4.isSelected())
				return Factor.PARTIAL_COLUMNS;
			return allRows;
		}
		public String getName() {
			return nameField.getText();
		}
		public int getRowCard() {
			return rowField.intValue();
		}
		public int getColCard() {
			return colField.intValue();
		}
		public void reset() {
			nameField.setText("");
		}
	}
	private static final int allRows = 0;
	private static final int allCols = 1;
	private static final int rows = 2;
	private static final int cols = 3;

	class AtomSelectionPane extends JPanel {
		ToggleListsBis toggleLists = new ToggleListsBis(null, null, unselectedLabel, selectedLabel);
		public AtomSelectionPane(Object[] t) {
			toggleLists.initModels(t, null);
			add(toggleLists);
		}
		public AtomSelectionPane() {
			this(null);
		}
		public void reset(Object[] t) {
			toggleLists.initModels(t, null);
		}
		public Object[] getSelected() {
			return toggleLists.getList2();
		}
		public Object[] getUnselected() {
			return toggleLists.getList1();
		}
	}
	
	class MyPropertyChangeListener implements PropertyChangeListener {
		public MyPropertyChangeListener() {
		}
		public void propertyChange(PropertyChangeEvent e) {
			String prop = e.getPropertyName();
			Object[] message = new Object[2];
			if (isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY) || prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
				Object value = optionPane.getValue();
				if (value == JOptionPane.UNINITIALIZED_VALUE) {
					//ignore reset
	 				return;
				}
				if (value.equals(okLabel)) {
					switch(state) {
					case stepNumber :
						curFactor = 1;
						nFactors = numberfactorPropPane.getValue();
						anova = new ANOVADesign(nFactors);
						state = stepFactor;
						message[0] = factorTitle+curFactor;
						message[1] = factorPropPane;
						optionPane.setMessage(message);
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						break;
					case stepFactor :
						switch(factorPropPane.getState()) {
						case Factor.ALL_ROWS :
							factor = new Factor(factorPropPane.getName(), factorPropPane.getState(), 0);
							anova.setFactor(curFactor-1, factor);
							if(curFactor<nFactors) {
								curFactor++;
								message[0] = factorTitle+curFactor;
								factorPropPane.reset();
								message[1] = factorPropPane;
								optionPane.setMessage(message);
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							} else {
								setOk();
							}
							break;
						case Factor.ALL_COLUMNS :
							factor = new Factor(factorPropPane.getName(), factorPropPane.getState(), 0);
							anova.setFactor(curFactor-1, factor);
							if(curFactor<nFactors) {
								curFactor++;
								state = stepFactor;
								message[0] = factorTitle+curFactor;
								factorPropPane.reset();
								message[1] = factorPropPane;
								optionPane.setMessage(message);
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							} else {
								setOk();
							}
							break;
						case Factor.PARTIAL_ROWS :
							factor = new Factor(factorPropPane.getName(), factorPropPane.getState(), factorPropPane.getRowCard());
							state = stepAtom;
							nAtoms = factorPropPane.getRowCard();
							curAtom = 1;
							atomSelectionPane.reset(tableExp.getRowLabelsIndexCopy());
							message[0] = factorTitle+factor.getName()+" : "+atomTitle+curAtom;
							message[1] = atomSelectionPane;
							optionPane.setMessage(message);
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							break;
						case Factor.PARTIAL_COLUMNS :
							factor = new Factor(factorPropPane.getName(), factorPropPane.getState(), factorPropPane.getColCard());
							state = stepAtom;
							nAtoms = factorPropPane.getColCard();
							curAtom = 1;
							atomSelectionPane.reset(tableExp.getColLabelsIndexCopy());
							message[0] = factorTitle+factor.getName()+" : "+atomTitle+curAtom;
							message[1] = atomSelectionPane;
							optionPane.setMessage(message);
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						};
						break;
					case stepAtom :
						Object[] sel = atomSelectionPane.getSelected(), unsel = atomSelectionPane.getUnselected();
						int[] index = new int[sel.length];
						for(int i=0; i<sel.length; i++)
							index[i] = ((LabelIndex) sel[i]).getIndex();
						factor.setAtom(curAtom-1, index);
						if(curAtom<nAtoms-1) {
							curAtom++;
							atomSelectionPane.reset(unsel);
							message[0] = factorTitle+factor.getName()+" : "+atomTitle+curAtom;
							message[1] = atomSelectionPane;
							optionPane.setMessage(message);
							optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						} else {
							index = new int[unsel.length];
							for(int i=0; i<unsel.length; i++)
								index[i] = ((LabelIndex) unsel[i]).getIndex();
							factor.setAtom(curAtom, index);
							anova.setFactor(curFactor-1, factor);
							if(curFactor<nFactors) {
								curFactor++;
								state = stepFactor;
								message[0] = factorTitle+curFactor;
								factorPropPane.reset();
								message[1] = factorPropPane;
								optionPane.setMessage(message);
								optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							} else {
								setOk();
							}
						}
						break;
					}
				} else { // user closed dialog or clicked cancel
					anova = null;
					setVisible(false);
				}
			}
		}
	}
	
	public void setOk() {
		if(setAction != null)
			setAction.actionPerformed(new ActionEvent(this, 0, "set"));
		setVisible(false);
	}
	
	public ANOVADesign getANOVA() {
		return anova;
	}
}

