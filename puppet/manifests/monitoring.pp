class document_store_api::monitoring {

    nagios::nrpe_checks::check_http { "${::certname}/1":
        url => "http://${::hostname}/healthcheck",
        port => 8081,
        expect => '....healthy..true',
        notes  => 'Default Note',
        notes_url => 'https://sites.google.com/a/ft.com/technology/',
        size => "20",
        wtime => 6,
        ctime => 30;
    }
}

