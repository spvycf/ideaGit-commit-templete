package commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import git4idea.GitLocalBranch;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Enumeration;

/**
 * @author Damien Arrachequesne
 */
public class CommitPanel {
    private JPanel mainPanel;
    private JComboBox<String> changeScope;
    private JTextField shortDescription;
    private JTextArea longDescription;
    private JTextArea breakingChanges;
    private JTextField closedIssues;
    private JCheckBox wrapTextCheckBox;
    private JCheckBox skipCICheckBox;
    private JRadioButton featRadioButton;
    private JRadioButton fixRadioButton;
    private JRadioButton docsRadioButton;
    private JRadioButton styleRadioButton;
    private JRadioButton refactorRadioButton;
    private JRadioButton perfRadioButton;
    private JRadioButton testRadioButton;
    private JRadioButton buildRadioButton;
    private JRadioButton ciRadioButton;
    private JRadioButton choreRadioButton;
    private JRadioButton revertRadioButton;
    private JRadioButton mergeBranchRadioButton;
    private JCheckBox addBranchNameCheckBox;
    private ButtonGroup changeTypeGroup;


    CommitPanel(Project project, CommitMessage commitMessage) {
        File workingDirectory = new File(project.getBasePath());
        GitLogQuery.Result result = new GitLogQuery(workingDirectory).execute();
        if (result.isSuccess()) {
            changeScope.addItem(""); // no value by default
            result.getScopes().forEach(changeScope::addItem);
        }

        if (commitMessage != null) {
            restoreValuesFromParsedCommitMessage(commitMessage);
        }
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    CommitMessage getCommitMessage(@Nullable Project project) {
        String branchName="";
        if (addBranchNameCheckBox.isSelected()){
            branchName=getBranchName(project);
        }
        return new CommitMessage(
                getSelectedChangeType(),
                (String) changeScope.getSelectedItem(),
                shortDescription.getText().trim(),
                longDescription.getText().trim(),
                breakingChanges.getText().trim(),
                closedIssues.getText().trim(),
                wrapTextCheckBox.isSelected(),
                skipCICheckBox.isSelected(),
                branchName
        );
    }

    private ChangeType getSelectedChangeType() {
        for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return ChangeType.valueOf(button.getActionCommand().toUpperCase());
            }
        }
        return null;
    }

    private void restoreValuesFromParsedCommitMessage(CommitMessage commitMessage) {
        if (commitMessage.getChangeType() != null) {
            for (Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements();) {
                AbstractButton button = buttons.nextElement();

                if (button.getActionCommand().equalsIgnoreCase(commitMessage.getChangeType().label())) {
                    button.setSelected(true);
                }
            }
        }
        changeScope.setSelectedItem(commitMessage.getChangeScope());
        shortDescription.setText(commitMessage.getShortDescription());
        longDescription.setText(commitMessage.getLongDescription());
        breakingChanges.setText(commitMessage.getBreakingChanges());
        closedIssues.setText(commitMessage.getClosedIssues());
        skipCICheckBox.setSelected(commitMessage.isSkipCI());
        addBranchNameCheckBox.setSelected(StringUtils.isNotEmpty(commitMessage.getBranchName()));
    }

    private String getBranchName(Project project) {
        String branch = "";
        ProjectLevelVcsManager instance = ProjectLevelVcsManagerImpl.getInstance(project);
        if (instance.checkVcsIsActive("Git")) {
            GitRepository currentRepository = GitBranchUtil.getCurrentRepository(project);
            if (currentRepository != null) {
                GitLocalBranch currentBranch = currentRepository.getCurrentBranch();
                if (currentBranch != null) {
                    branch = currentBranch.getName().trim();
                }
            }
        }
        return branch;
    }
}
