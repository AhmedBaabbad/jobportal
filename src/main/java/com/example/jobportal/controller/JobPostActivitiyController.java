package com.example.jobportal.controller;

import com.example.jobportal.model.*;
import com.example.jobportal.service.JobPostActivityService;
import com.example.jobportal.service.JobSeekerApplyService;
import com.example.jobportal.service.JobSeekerSaveService;
import com.example.jobportal.service.UsersService;
import com.example.jobportal.util.ConstantsUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
public class JobPostActivitiyController {

    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;
    private final JobSeekerApplyService jobSeekerApplyService;
    private final JobSeekerSaveService jobSeekerSaveService;

    public JobPostActivitiyController(UsersService usersService,
                                      JobPostActivityService jobPostActivityService,
                                      JobSeekerApplyService jobSeekerApplyService,
                                      JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @GetMapping("/dashboard/")
    public String searchJobs(Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) boolean today,
                             @RequestParam(value = "days7", required = false) boolean days7,
                             @RequestParam(value = "days30", required = false) boolean days30

    ) {

        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(partTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(partTime, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(partTime, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(partTime, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partTime, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }

        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if (officeOnly == null && remoteOnly == null && partialRemote == null) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }

        if (!dateSearchFlag && !remote && !type && !StringUtils.hasText(job) && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            jobPost = jobPostActivityService.search(job, location, Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote), searchDate);
        }
        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(ConstantsUtil.ROLE_RECRUITER))) {
                List<RecruiterJobDto> recruiterJobs = jobPostActivityService.getRecruiterJob(((RecruiterProfile) currentUserProfile).getUserAccountId());
                model.addAttribute("jobPost", recruiterJobs);
            } else {
                List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidateJobs((JobSeekerProfile) currentUserProfile);
                List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidateJobs((JobSeekerProfile) currentUserProfile);

                boolean exist;
                boolean saved;

                for (JobPostActivity jobActivity : jobPost) {
                    exist = false;
                    saved = false;
                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                        if (Objects.equals(jobActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())) {
                            jobActivity.setIsActive(true);
                            exist = true;
                            break;
                        }
                    }

                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (Objects.equals(jobActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId())) {
                            jobActivity.setIsSaved(true);
                            saved = true;
                            break;
                        }
                    }

                    if (!exist) {
                        jobActivity.setIsActive(false);
                    }
                    if (!saved) {
                        jobActivity.setIsSaved(false);
                    }

                    model.addAttribute("jobPost", jobPost);

                }
            }
        }

        model.addAttribute("user", currentUserProfile);

        return "dashboard";
    }

      /*  Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            model.addAttribute("username", currentUsername);

            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(ConstantsUtil.ROLE_RECRUITER))) {
                // Check if currentUserProfile is an instance of RecruiterProfile
                if (currentUserProfile instanceof RecruiterProfile) {
                    RecruiterProfile recruiterProfile = (RecruiterProfile) currentUserProfile;
                    List<RecruiterJobDto> recruiterJob = jobPostActivityService.getRecruiterJob(recruiterProfile.getUserAccountId());
                    model.addAttribute("jobPost", recruiterJob);
                }
                else {
                    List<JobSeekerApply> jobSeekerApplyList = jobSeekerApplyService.getCandidateJobs(
                            (JobSeekerProfile) currentUserProfile);
                    List<JobSeekerSave> jobSeekerSaveList = jobSeekerSaveService.getCandidateJobs(
                            (JobSeekerProfile) currentUserProfile);
                    boolean exist;
                    boolean saved;
                    for (JobPostActivity jobPostActivity : jobPost) {
                        exist = false;
                        saved = false;
                        for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                            if (Objects.equals(jobPostActivity.getJobPostId(), jobSeekerApply.getJob().getJobPostId())) {
                                jobPostActivity.setIsActive(true);
                                exist = true;
                                saved = true;
                                break;
                            }
                        }
                        for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                            if (Objects.equals(jobPostActivity.getJobPostId(), jobSeekerSave.getJob().getJobPostId())) {
                                jobPostActivity.setIsSaved(true);
                                exist = true;
                                saved = true;
                                break;
                            }
                        }
                        if (!exist) {
                            jobPostActivity.setIsActive(false);
                        }
                        if (!saved) {
                            jobPostActivity.setIsSaved(false);
                        }
                        model.addAttribute("jobPost", jobPost);
                    }
                }

            }
        }

        // Add the user profile to the model regardless of the role
        model.addAttribute("user", currentUserProfile);

        return "dashboard";
    } */

    @GetMapping("/dashboard/add")
    public String addJob(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());
        return "add-jobs";
    }

    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {
        Users user = usersService.getCurrentUser();
        if (user != null) {
            jobPostActivity.setPostedById(user);
        }
        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("JobPostActivity", jobPostActivity);
        JobPostActivity savedJobPost = jobPostActivityService.addNew(jobPostActivity);
        return "redirect:/dashboard/";
    }
}
