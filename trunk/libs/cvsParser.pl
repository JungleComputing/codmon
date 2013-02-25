#!/usr/bin/perl

use strict;

`export CVS_RSH="ssh"`;

my $scope = $ARGV[0];
if (!($scope eq "")) {
    $scope = "-p $scope";
}

#my $scope = "-p codmon";

my @out = `cvs -d :ext:codmon\@fs0.das2.cs.vu.nl:/usr/local/proj/ibis/Repository history -al -x M -D "1 days ago" $scope`;

foreach my $line (@out) {
    if ($line =~ m/M\s*(\S*)\s*(\S*)\s*(\S*)\s*(\S*)\s*(\S*)\s*(\S*)\s*(\S*)/) {

	my $date = $1." ".$2." ".$3;
	my $user = $4;
	my $file = $7."/".$6;

	print("<modif date=\"".$date."\" user=\"".$user."\" file=\"".$file."\"/>");

    }
}

