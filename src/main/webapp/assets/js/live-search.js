(function () {
    const DEFAULT_DEBOUNCE_MS = 180;

    const normalizeText = (value) => (value || '')
        .toString()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();

    const debounce = (fn, waitMs) => {
        let timerId;
        return (...args) => {
            window.clearTimeout(timerId);
            timerId = window.setTimeout(() => fn(...args), waitMs);
        };
    };

    window.EngineGalleryLiveSearch = {
        normalizeText,
        init(options) {
            if (!options || !options.input) {
                return null;
            }

            const input = typeof options.input === 'string'
                ? document.querySelector(options.input)
                : options.input;
            if (!input) {
                return null;
            }

            const groups = Array.isArray(options.groups) ? options.groups : [];
            const emptyState = options.emptyState
                ? (typeof options.emptyState === 'string' ? document.querySelector(options.emptyState) : options.emptyState)
                : null;
            const debounceMs = Number.isFinite(options.debounceMs) ? options.debounceMs : DEFAULT_DEBOUNCE_MS;
            const showEmptyWhenBlank = options.showEmptyWhenBlank === true;

            const apply = () => {
                const keyword = normalizeText(input.value);
                let visibleCount = 0;

                groups.forEach((group) => {
                    let elements = [];
                    if (typeof group.selector === 'string') {
                        elements = Array.from(document.querySelectorAll(group.selector));
                    } else if (group.elements && typeof group.elements.forEach === 'function') {
                        elements = Array.from(group.elements);
                    }

                    elements.forEach((element) => {
                        const haystack = normalizeText(element.dataset.search);
                        const isVisible = keyword.length === 0 || haystack.includes(keyword);
                        element.classList.toggle('d-none', !isVisible);
                        if (!group.skipCount && isVisible) {
                            visibleCount += 1;
                        }
                    });
                });

                if (emptyState) {
                    const showEmpty = visibleCount === 0 && (showEmptyWhenBlank || keyword.length > 0);
                    emptyState.classList.toggle('d-none', !showEmpty);
                }

                if (typeof options.onAfterFilter === 'function') {
                    options.onAfterFilter({ keyword, visibleCount });
                }

                return { keyword, visibleCount };
            };

            const debouncedApply = debounce(apply, debounceMs);
            input.addEventListener('input', debouncedApply);

            return {
                apply,
                applyDebounced: debouncedApply,
                normalizeText
            };
        }
    };
})();
