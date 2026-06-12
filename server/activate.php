<?php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
$email = trim($input['email'] ?? '');

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid email address']);
    exit;
}

$file = 'emails.txt';
$line = date('Y-m-d H:i:s') . ' | ' . $email . "\n";
file_put_contents($file, $line, FILE_APPEND | LOCK_EX);

echo json_encode(['success' => true, 'message' => 'Activation successful']);
