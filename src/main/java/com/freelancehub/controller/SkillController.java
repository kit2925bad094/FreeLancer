package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.model.Skill;
import com.freelancehub.service.SkillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ArrayList<Skill>>> getAll() {
        ArrayList<Skill> skills = skillService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.success("Skills retrieved", skills));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Skill>> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        Skill skill = skillService.createSkill(name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created", skill));
    }
}
