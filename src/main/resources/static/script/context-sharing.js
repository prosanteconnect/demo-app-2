let pscContext;
const mappingUrl = window.location.origin + '/patient-info-mapping.json'

function getFromCache(serverUrl) {
    $.get(serverUrl, function (data) {
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

function putInCache(schemaName, serverUrl, viewURL) {
    let putPscContext = {};

    $.getJSON(mappingUrl, function (data) {
        for (const [key, value] of Object.entries(data)) {
            if (document.getElementById(key)) {
                _.set(putPscContext, value, document.getElementById(key).value)
            }
        }
        _.set(putPscContext, "schemaId", schemaName)

        $.ajax({
            url: serverUrl,
            type: 'PUT',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(putPscContext)
        })
            .done(function(data) { window.location.href=viewURL })
            .fail(function(jqXHR, textStatus, errorThrown) {
                window.location.href=viewURL
            })
    })
}


