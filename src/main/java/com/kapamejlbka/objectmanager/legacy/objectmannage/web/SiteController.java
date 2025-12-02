package com.kapamejlbka.objectmanager.legacy.objectmannage.web;

import com.kapamejlbka.objectmanager.legacy.objectmannage.domain.Site;
import com.kapamejlbka.objectmanager.legacy.objectmannage.service.SiteService;
import com.kapamejlbka.objectmanager.legacy.objectmannage.web.form.SiteForm;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public String index(
            @RequestParam(name = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            CsrfToken csrfToken
    ) {
        Page<Site> sitesPage = siteService.findPage(query, pageable);
        model.addAttribute("sitesPage", sitesPage);
        model.addAttribute("q", query);
        model.addAttribute("_csrf", csrfToken);
        return "sites/index";
    }

    @GetMapping("/new")
    public String createForm(Model model, CsrfToken csrfToken) {
        model.addAttribute("form", new SiteForm());
        model.addAttribute("isEdit", false);
        model.addAttribute("binding", null);
        model.addAttribute("_csrf", csrfToken);
        return "sites/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") SiteForm form,
            BindingResult bindingResult,
            Model model,
            CsrfToken csrfToken,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("binding", bindingResult);
            return "sites/form";
        }

        siteService.create(form);
        redirectAttributes.addFlashAttribute("flashSuccess", "Сохранено");
        return "redirect:/sites";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") UUID id, Model model, CsrfToken csrfToken) {
        Site site;
        try {
            site = siteService.get(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        model.addAttribute("form", SiteForm.fromSite(site));
        model.addAttribute("siteId", site.getId());
        model.addAttribute("isEdit", true);
        model.addAttribute("binding", null);
        model.addAttribute("_csrf", csrfToken);
        return "sites/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("form") SiteForm form,
            BindingResult bindingResult,
            Model model,
            CsrfToken csrfToken,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("siteId", id);
            model.addAttribute("_csrf", csrfToken);
            model.addAttribute("binding", bindingResult);
            return "sites/form";
        }

        try {
            siteService.update(id, form);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        redirectAttributes.addFlashAttribute("flashSuccess", "Сохранено");
        return "redirect:/sites";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            siteService.delete(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        redirectAttributes.addFlashAttribute("flashSuccess", "Удалено");
        return "redirect:/sites";
    }
}
