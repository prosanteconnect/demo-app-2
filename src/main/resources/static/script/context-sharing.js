let pscContext;
const mappingUrl = window.location.origin + '/patient-info-mapping.json'

function getFromCache() {
    $.get("/secure/share", function (data) {
        console.log(data)
        if (data !== null && data !== "") {
            pscContext = data;
            const btnPreFill = $("#btnPreFill");
            const contextTooltip = $('#contextTooltip')

            btnPreFill.removeAttr("hidden");
            contextTooltip.removeAttr('hidden')
            document.getElementById('contextTooltip').setAttribute(
                'title', JSON.stringify(pscContext, null, 2))
        }
    });
}

function fillForm() {
    $.getJSON(mappingUrl, function(data) {
        for (const [key, value] of Object.entries(data)) {
            if (document.getElementById(key)) {
                $('#' + key).val(_.get(pscContext, value, ''))
            }
        }
    })
}

function putInCache(schemaName, viewURL) {
    const csrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)CSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
    const xsrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
    let putPscContext = {};

    $.getJSON(mappingUrl, function (data) {
        for (const [key, value] of Object.entries(data)) {
            if (document.getElementById(key)) {
                _.set(putPscContext, value, document.getElementById(key).value)
            }
        }
        _.set(putPscContext, "schemaId", schemaName)

        $.ajax({
            url: '/secure/share',
            type: 'PUT',
            headers: {'X-XSRF-TOKEN': xsrfToken, 'X-CSRF-TOKEN': csrfToken},
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(putPscContext)
        })
            .done(function(data) { window.location.href=viewURL })
            .fail(function(jqXHR, textStatus, errorThrown) {
                console.log(jqXHR)
                console.log(textStatus)
                console.log(errorThrown)
                // window.location.href=viewURL
            })
    })
}


