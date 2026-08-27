# Thyroid Echo navigation and optional-feature architecture

## Permanent primary navigation

Thyroid Echo should remain at five bottom destinations even as the app grows:

1. **Today** — things the user may act on today: medication status, quick check-in, symptoms, and enabled daily measurements/context.
2. **Journal** — the chronological record of what the user entered.
3. **Medication** — current medication, medication reminders, clinician-directed dose changes, and medication history.
4. **Insights** — trends, labs, comparisons, and doctor-ready reports.
5. **Settings** — personalization, accessibility, optional modules, privacy/data tools, backup/restore, and support/about.

Optional features must not create additional bottom-navigation tabs.

## Where optional features belong

Every optional feature has two parts:

- **Enable/configure it in Settings.**
- **Use it in the destination where the information naturally belongs.**

Examples:

| Optional module | Configure | Daily input / records | Review |
| --- | --- | --- | --- |
| Context tags | Settings | Today | Journal / Insights |
| Weight | Settings | Today | Journal / Insights |
| Expanded thyroid labs / antibodies | Settings | Insights → Labs | Insights / Doctor report |
| Custom symptoms | Settings | Today | Journal / Insights |
| Pulse / blood pressure / temperature | Settings | Today | Journal / Insights |
| Supplements | Settings | Today or Medication, depending on design | Journal / Insights |
| Appointment tracking | Settings | Insights / appointment card | Journal / Doctor report |
| Additional thyroid conditions / treatment states | Settings → Profile | Changes which relevant fields are offered | Throughout app |
| Encrypted backup / restore | Settings | Settings only | Settings only |

## Settings organization as the app grows

Keep Settings grouped into clear sections rather than one long list:

- **Profile & accessibility** — name, condition/profile options, text/control size.
- **Daily tracking** — custom symptoms, context tags, weight, optional measurements.
- **Labs & medical records** — expanded thyroid labs, antibody fields, appointments, supplements when added.
- **Reminders** — global reminder preferences if reminder types expand beyond medication. Medication-specific timing can remain in Medication.
- **Data & privacy** — encrypted backup/restore, future phone-to-phone transfer, privacy information.
- **About & support** — app information and voluntary Support Thyroid Echo contribution when implemented.

## UX rules

- The default install should remain light. Optional modules are opt-in unless already part of the established core experience.
- Turning a module off hides its inputs; it must not delete previously saved data.
- Enabling a module should reveal it only where relevant, not across every screen.
- Prefer progressive disclosure (cards, short sub-sections, expandable detail) over long forms.
- Do not gamify illness or use guilt-based streak mechanics.
- Keep medical interpretation out of the app; store and summarize user-entered information for the user and their clinician.
- Keep the daily path fast: a user who wants only medication + a basic check-in should never have to pass through advanced fields.

This structure is intended to let Thyroid Echo become comprehensive without becoming visually or cognitively heavy.
