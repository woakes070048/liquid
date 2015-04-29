package liquid.process.service;

import liquid.util.DatePattern;
import liquid.util.DateUtil;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Tao Ma on 4/4/15.
 */
@Service
public class ProcessServiceImpl implements ProcessService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    private String processDefinitionKey = "shipping";
    private String definitionClasspath = "processes/liquid.shipping.bpmn20.xml";

    @Autowired
    private ProcessEngine processEngine;

    @Override
    public void startProcess(String uid, BusinessKey businessKey, Map<String, Object> variableMap) {
        RuntimeService runtimeService = processEngine.getRuntimeService();

        deployProcess();

        variableMap.put("employeeName", uid);
        variableMap.put("endTime", DateUtil.stringOf(Calendar.getInstance().getTime(), DatePattern.UNTIL_SECOND));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey.getText(), variableMap);
        runtimeService.addUserIdentityLink(processInstance.getId(), uid, IdentityLinkType.STARTER);

        // FIXME email notification.
//        Account account = accountService.find(uid);
//        mailNotificationService.send(messageSource.getMessage("process.start", null, Locale.CHINA),
//                messageSource.getMessage("process.start.content", new String[]{uid}, Locale.CHINA),
//                account.getEmail());
    }

    private void deployProcess() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
        // If there are something changed in definition file located definitionClasspath, activiti will auto-deploy it.
        if (null == processDefinition)
            repositoryService.createDeployment().addClasspathResource(definitionClasspath).deploy();
    }

    @Override
    public void listProcessDefinitions(String key) {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().
                processDefinitionKey(key).orderByProcessDefinitionVersion().asc().list();
        for (ProcessDefinition processDefinition : processDefinitions) {
            System.out.println(processDefinition);
        }
    }
}
